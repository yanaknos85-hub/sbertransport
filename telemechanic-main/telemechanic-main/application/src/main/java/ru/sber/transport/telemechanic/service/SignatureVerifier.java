package ru.sber.transport.telemechanic.service;

public interface SignatureVerifier {

    void verify(byte[] fileAsBytes, String signature, String signerFullName);
}
