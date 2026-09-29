from faster_whisper import WhisperModel

model = WhisperModel("tiny", device="cpu", compute_type="int8")

def text(audio_file):
    audio_path = "temp_audio.wav"
    audio_file.save(audio_path)

    segments, info = model.transcribe(audio_path)

    transcript = ""

    for segment in segments:
        transcript += segment.text + " "

    return transcript.strip()