package com.example.emergencyapp.model;

public class Message {
    public String id;
    public String senderId;
    public String senderEmail;
    public String senderName;
    public String text;
    public long timestamp;
    public boolean emergency;

    public String attachedPatientUid;

    // Новые поля:
    public String patientId;           // если сообщение связано с пациентом
    public String medicalCardRef;      // путь к медкарте в Firebase или ссылке
    public String senderRole;          // "doctor", "patient", "admin"
    public String eventType;
    public String status; // тип события, например "падение", "сердечный приступ", "другое"

    // Пустой конструктор нужен Firebase
    public Message() {}

    public Message(String id,
                   String senderId,
                   String senderEmail,
                   String senderName,
                   String text,
                   long timestamp,
                   boolean emergency) {
        this.id = id;
        this.senderId = senderId;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.emergency = emergency;
        this.status = "new";
    }

    // Расширенный конструктор (опционально)
    public Message(String id,
                   String senderId,
                   String senderEmail,
                   String senderName,
                   String text,
                   long timestamp,
                   boolean emergency,
                   String patientId,
                   String medicalCardRef,
                   String senderRole,
                   String eventType) {
        this(id, senderId, senderEmail, senderName, text, timestamp, emergency);
        this.patientId = patientId;
        this.medicalCardRef = medicalCardRef;
        this.senderRole = senderRole;
        this.eventType = eventType;
        this.status = "new";
    }

    // Геттеры и сеттеры (при необходимости)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getMedicalCardRef() { return medicalCardRef; }
    public void setMedicalCardRef(String medicalCardRef) { this.medicalCardRef = medicalCardRef; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}
