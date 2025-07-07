package mvc;

public enum FileType {
    PDF("pdf", "application/pdf"),
    JPEG("jpeg", "image/jpeg"),
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    TXT("txt", "text/plain"),
    CSV("csv", "text/csv"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    MP4("mp4", "video/mp4"),
    MP3("mp3", "audio/mpeg"),
    ZIP("zip", "application/zip"),
    JSON("json", "application/json"),
    XML("xml", "application/xml"),
    UNKNOWN("bin", "application/octet-stream"),
    IMAGE("image", "image/*");

    private String extension;
    private String mimeType;

    FileType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static boolean contains(String mimeType) {
        for (FileType type : values()) {
            // Exact match
            if (type.mimeType.equalsIgnoreCase(mimeType)) {
                return true;
            }
            // Wildcard match
            if (type.mimeType.endsWith("/*")) {
                String prefix = type.mimeType.substring(0, type.mimeType.length() - 1);
                if (mimeType.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static FileType fromExtension(String ext) {
        for (FileType type : values()) {
            if (type.extension.equalsIgnoreCase(ext)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static FileType fromContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return UNKNOWN; // Handle null or empty input gracefully
        }
        String trimmedContentType = contentType.trim();
        for (FileType type : values()) {
            // Skip the generic IMAGE type in the main loop if you want exact matches first
            if (type == IMAGE)
                continue;

            if (type.mimeType.equalsIgnoreCase(trimmedContentType)) {
                return type;
            }
        }
        // If no exact match, check for the generic image type
        if (trimmedContentType.equalsIgnoreCase(IMAGE.getMimeType()) || trimmedContentType.startsWith("image/")) {
            return IMAGE;
        }

        // Add similar checks for other potential generic types if needed (e.g.,
        // "text/*", "audio/*")

        return UNKNOWN; // Return UNKNOWN if no specific or known generic match found
    }
}
