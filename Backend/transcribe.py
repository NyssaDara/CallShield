from faster_whisper import WhisperModel

model = WhisperModel('small')

def text(audio_file):
    audio_path = "temp_audio.wav"
    audio_file.save(audio_path)  # FileStorage has .save(), not .getvalue()

    segments, info = model.transcribe(audio_path)

    transcript = ""
    for segment in segments:
        transcript += segment.text + " "

    return transcript.strip()