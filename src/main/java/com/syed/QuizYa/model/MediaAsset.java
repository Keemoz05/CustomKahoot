package com.syed.QuizYa.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ENTITY: MediaAsset
 *
 * Represents a single uploaded image or video managed via Cloudinary.
 * One MediaAsset can be attached to one Question (questions.media_asset_id FK).
 *
 * The 'uploaded_by' FK (pointing to users.id) is deferred to Week 7 when
 * host authentication is added.
 */
@Entity
@Table(name = "media_assets")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cloudinary's unique identifier for this asset — used to build transformation URLs
    @Column(name = "cloudinary_public_id", length = 255, nullable = false, unique = true)
    private String cloudinaryPublicId;

    // Full URL to the original file as uploaded
    @Column(name = "original_url", length = 500, nullable = false)
    private String originalUrl;

    // URL with center-crop transformation applied (width=800, height=450, crop=fill)
    @Column(name = "transformed_url", length = 500)
    private String transformedUrl;

    // "IMAGE" or "VIDEO"
    @Column(name = "media_type", length = 10, nullable = false)
    private String mediaType;

    // Original file size in bytes — nullable, used for quota tracking
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // Accessibility alt text for screen readers — nullable
    @Column(name = "alt_text", length = 300)
    private String altText;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getTransformedUrl() { return transformedUrl; }
    public void setTransformedUrl(String transformedUrl) { this.transformedUrl = transformedUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
