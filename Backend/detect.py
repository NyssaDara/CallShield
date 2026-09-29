def detection(transcript):
    scam = 0
    transcript_lower = transcript.lower()  # fix case-sensitivity

    fraud_words = [
        "digital arrest", "you are under arrest", "cyber crime", "police",
        "cbi", "ed", "narcotics", "money laundering", "criminal case", "fir",
        "warrant", "court order", "legal action", "police case",
        "criminal investigation", "your aadhaar is linked",
        "your aadhaar has been misused", "your number is involved",
        "your account is involved", "otp", "one time password",
        "verification code", "security code", "cvv", "pin", "atm pin",
        "upi pin", "bank account", "account number", "card number",
        "debit card", "credit card", "net banking", "bank details",
        "verify your account", "verify your identity",
        "account will be blocked", "account will be frozen",
        "account is suspended", "kyc", "invest", "investment",
        "guaranteed returns", "guaranteed profit", "double your money",
        "double the amount", "high returns", "huge returns", "quick profit",
        "easy money", "risk free", "zero risk", "trading account",
        "trading tips", "stock tips", "crypto investment", "cryptocurrency",
        "crypto trading", "limited time investment",
        "special investment opportunity", "lottery", "you won",
        "congratulations", "lucky draw", "prize", "reward", "cash prize",
        "winner", "claim your prize", "processing fee", "registration fee",
        "tax payment", "release the prize", "winning amount"
    ]

    for word in fraud_words:
        if word in transcript_lower:
            scam += 1

    if scam >= 5:
        return "HIGH RISK CALL DETECTED"
    elif scam >= 2:
        return "RISK CALL DETECTED..ASKED FOR INFO"
    else:
        return "LOW RISK CALL"