
def detection(transcript):
    transcript_lower = (transcript or "").lower().strip()

    if not transcript_lower:
        return {
            "score": 0,
            "level": "UNKNOWN",
            "reasons": ["No clear speech was detected. Please try another recording."]
        }

    # Phrase and behaviour indicators with heuristic weights.
    # These weights are NOT probabilities.
    indicators = {
        "digital arrest": 30,
        "you are under arrest": 30,
        "money laundering": 15,
        "criminal investigation": 20,
        "your aadhaar has been misused": 20,
        "your aadhaar is linked": 15,
        "account will be frozen": 20,
        "account will be blocked": 15,
        "account is suspended": 15,
        "share your otp": 30,
        "tell me your otp": 30,
        "one time password": 15,
        "verification code": 10,
        "share your pin": 30,
        "upi pin": 20,
        "atm pin": 20,
        "cvv": 20,
        "bank details": 15,
        "verify your account": 10,
        "processing fee": 15,
        "claim your prize": 15,
        "guaranteed returns": 15,
        "double your money": 20,
        "zero risk investment": 20,
        "remote access": 15,
        "install this app": 10,
        "send money immediately": 25,
        "transfer the money": 15,
        "keep this confidential": 15,
        "do not tell anyone": 15,
        "act immediately": 10,
        "limited time": 5,
        "police": 5,
        "cbi": 5,
        "cyber crime": 10,
        "kyc": 5,
        "lottery": 5,
    }

    matched = [
        (phrase, weight)
        for phrase, weight in indicators.items()
        if phrase in transcript_lower
    ]

    # Combine evidence, with a maximum score of 100.
    score = min(100, sum(weight for _, weight in matched))

    if score >= 60:
        level = "HIGH"
    elif score >= 30:
        level = "MEDIUM"
    else:
        level = "LOW"

    reasons = [
        f"Suspicious phrase detected: '{phrase}'"
        for phrase, _ in matched
    ]

    if not reasons:
        reasons.append("No listed scam indicators were detected.")

    return {
        "score": score,
        "level": level,
        "reasons": reasons
    }
