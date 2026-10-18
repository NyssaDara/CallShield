
from flask import Flask, request, jsonify
import mysql.connector
import os
import tempfile

from transcribe import text
from detect import detection

app = Flask(__name__)

# Maximum audio upload size: 25 MB
app.config["MAX_CONTENT_LENGTH"] = 25 * 1024 * 1024


# =========================
# MYSQL CONNECTION
# =========================

def get_connection():
    return mysql.connector.connect(
        host=os.getenv("DB_HOST"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_NAME")
    )


# =========================
# CHECK PHONE NUMBER
# =========================

def check_phone(phone):
    connection = get_connection()

    try:
        cursor = connection.cursor(dictionary=True)

        try:
            cursor.execute(
                """
                SELECT
                    Phn_no,
                    Blocked_count,
                    Reported_count,
                    Help_count
                FROM data
                WHERE Phn_no = %s
                """,
                (phone,)
            )

            return cursor.fetchone()

        finally:
            cursor.close()

    finally:
        connection.close()


@app.route("/check", methods=["GET"])
def check():
    phone = request.args.get("phone", "").strip()

    if not phone:
        return jsonify({
            "error": "phone parameter is required"
        }), 400

    try:
        result = check_phone(phone)

        if result:
            return jsonify(result)

        return jsonify({
            "message": "not found",
            "phone": phone,
            "Reported_count": 0,
            "Blocked_count": 0,
            "Help_count": 0
        }), 200

    except Exception:
        app.logger.exception("Phone check failed")

        return jsonify({
            "error": "Unable to check phone number"
        }), 500


# =========================
# USER ACTION
# =========================

@app.route("/action", methods=["POST"])
def action():
    data = request.get_json(silent=True)

    if not data:
        return jsonify({
            "error": "JSON body required"
        }), 400

    phone = str(data.get("phone", "")).strip()
    action_name = data.get("action")

    if not phone or not action_name:
        return jsonify({
            "error": "phone and action are required"
        }), 400

    valid_columns = {
        "block": "Blocked_count",
        "report": "Reported_count",
        "help": "Help_count"
    }

    if action_name not in valid_columns:
        return jsonify({
            "error": "Invalid action"
        }), 400

    column = valid_columns[action_name]

    connection = None
    cursor = None

    try:
        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute(
            f"""
            INSERT INTO data (Phn_no, {column})
            VALUES (%s, 1)
            ON DUPLICATE KEY UPDATE
                {column} = {column} + 1
            """,
            (phone,)
        )

        connection.commit()

        return jsonify({
            "status": "ok",
            "phone": phone,
            "action": action_name
        }), 200

    except Exception:
        if connection:
            connection.rollback()

        app.logger.exception("User action failed")

        return jsonify({
            "error": "Unable to save user action"
        }), 500

    finally:
        if cursor:
            cursor.close()

        if connection:
            connection.close()


# =========================
# AUDIO ANALYSIS
# =========================

@app.route("/analyze", methods=["POST"])
def analyze():
    audio_file = request.files.get("audio")

    if not audio_file or not audio_file.filename:
        return jsonify({
            "error": "Please upload an audio file using the 'audio' field"
        }), 400

    temp_path = None

    try:
        # Save the uploaded audio to a temporary file.
        # This avoids using the same temp filename for concurrent requests.
        suffix = os.path.splitext(audio_file.filename)[1] or ".wav"

        with tempfile.NamedTemporaryFile(
            suffix=suffix,
            delete=False
        ) as temp_file:
            temp_path = temp_file.name
            audio_file.save(temp_path)

        # Transcribe the audio.
        # transcribe.py must accept a file path for this version.
        transcript = text(temp_path)

        if not transcript or not transcript.strip():
            return jsonify({
                "error": "No speech could be transcribed. Try a clearer recording."
            }), 422

        # Analyze the transcript.
        risk = detection(transcript)

        return jsonify({
            "transcript": transcript,
            "risk": risk
        }), 200

    except Exception:
        app.logger.exception("Audio analysis failed")

        return jsonify({
            "error": "Audio analysis failed. Please check the audio format and backend logs."
        }), 500

    finally:
        if temp_path and os.path.exists(temp_path):
            try:
                os.remove(temp_path)
            except OSError:
                app.logger.warning("Could not remove temporary audio file")


# =========================
# UPLOAD ERROR HANDLER
# =========================

@app.errorhandler(413)
def file_too_large(error):
    return jsonify({
        "error": "Audio file is too large. Maximum size is 25 MB."
    }), 413


# =========================
# HEALTH CHECK
# =========================

@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "app": "CallShield API",
        "status": "running",
        "endpoints": ["/check", "/action", "/analyze"]
    }), 200


# =========================
# START SERVER
# =========================

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))

    app.run(
        host="0.0.0.0",
        port=port,
        debug=False
    )
