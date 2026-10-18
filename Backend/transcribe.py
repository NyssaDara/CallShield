
from faster_whisper import WhisperModel

model = WhisperModel(
    "tiny",
    device="cpu",
    compute_type="int8"
)


def text(audio_path):
    segments, info = model.transcribe(audio_path)

    transcript = " ".join(
        segment.text.strip()
        for segment in segments
    ).strip()

    return transcript
