from flask import Flask, request, jsonify
import mysql.connector
import os

from transcribe import text
from detect import detection

app = Flask(__name__)


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
    cursor = connection.cursor(dictionary=True)

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

    result = cursor.fetchone()

    cursor.close()
    connection.close()

    return result


@app.route("/check", methods=["GET"])
def check():

    phone = request.args.get("phone")

    if not phone:

        return jsonify({
            "error": "phone parameter is required"
        }), 400

    result = check_phone(phone)

    if result:

        return jsonify(result)

    return jsonify({
        "message": "not found"
    })


# =========================
# USER ACTION
# =========================

@app.route("/action", methods=["POST"])
def action():

    data = request.json

    if not data:

        return jsonify({
            "error": "JSON body required"
        }), 400

    if "phone" not in data or "action" not in data:

        return jsonify({
            "error": "phone and action are required"
        }), 400

    phone = data["phone"]
    action = data["action"]

    valid_columns = {

        "block": "Blocked_count",
        "report": "Reported_count",
        "help": "Help_count"

    }

    if action not in valid_columns:

        return jsonify({
            "error": "Invalid action"
        }), 400

    column = valid_columns[action]

    connection = get_connection()
    cursor = connection.cursor()

    try:

        cursor.execute(
            f"""
            INSERT INTO data
                (Phn_no, {column})
            VALUES
                (%s, 1)

            ON DUPLICATE KEY UPDATE
                {column} = {column} + 1
            """,
            (phone,)
        )

        connection.commit()

    finally:

        cursor.close()
        connection.close()

    return jsonify({
        "status": "ok"
    })


# =========================
# AUDIO ANALYSIS
# =========================

@app.route("/analyze", methods=["POST"])
def analyze():

    audio_file = request.files.get("audio")

    if not audio_file:

        return jsonify({
            "error": "audio file is required"
        }), 400

    transcript = text(audio_file)

    risk = detection(transcript)

    return jsonify({

        "transcript": transcript,
        "risk": risk

    })


# =========================
# START SERVER
# =========================

if __name__ == "__main__":

    app.run(
        host="0.0.0.0",
        port=5000
    )