package cami.hash;

public interface IHashAlgorithm {
    String getFingerprint(String path);
    String getFingerprint(String path, boolean hideVersion);
}
