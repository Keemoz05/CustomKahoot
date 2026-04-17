package com.syed.QuizYa.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.syed.QuizYa.model.MediaAsset;
import com.syed.QuizYa.repository.MediaAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * SERVICE: MediaService
 *
 * Handles the full lifecycle of a media upload:
 *   1. Receive a MultipartFile from the controller
 *   2. Push it to Cloudinary (image or video)
 *   3. Build a center-crop transformation URL (no face detection)
 *   4. Save a MediaAsset record to the database
 *   5. Return the saved entity (with URLs) back to the controller
 */
@Service
public class MediaService {

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;

    public MediaService(Cloudinary cloudinary, MediaAssetRepository mediaAssetRepository) {
        this.cloudinary = cloudinary;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    /**
     * Uploads a file to Cloudinary and persists a MediaAsset record.
     *
     * @param file The image or video uploaded by the host
     * @return The saved MediaAsset containing originalUrl and transformedUrl
     * @throws IOException if the file cannot be read or Cloudinary upload fails
     */
    @Transactional
    public MediaAsset upload(MultipartFile file) throws IOException {
        // Determine whether this is an image or video based on the MIME type
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        String resourceType = contentType.startsWith("video/") ? "video" : "image";
        String mediaType = contentType.startsWith("video/") ? "VIDEO" : "IMAGE";

        // Upload the raw bytes to Cloudinary
        // resource_type "auto" lets Cloudinary detect images vs videos automatically
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "quizya/media",
                        "resource_type", "auto"
                )
        );

        // Extract the values Cloudinary returns after a successful upload
        String publicId = (String) uploadResult.get("public_id");
        String originalUrl = (String) uploadResult.get("secure_url");
        Object sizeObj = uploadResult.get("bytes");
        Long fileSize = sizeObj != null ? Long.parseLong(sizeObj.toString()) : null;

        // Build a center-crop transformation URL (800x450, no face detection).
        // Cloudinary processes this transformation on the fly via URL parameters —
        // the original file is never modified.
        String transformedUrl = buildCenterCropUrl(publicId, resourceType);

        // Persist a MediaAsset record so the question can reference it via FK
        MediaAsset asset = new MediaAsset();
        asset.setCloudinaryPublicId(publicId);
        asset.setOriginalUrl(originalUrl);
        asset.setTransformedUrl(transformedUrl);
        asset.setMediaType(mediaType);
        asset.setFileSizeBytes(fileSize);

        return mediaAssetRepository.save(asset);
    }

    /**
     * Constructs a Cloudinary URL that center-crops the asset to 800x450.
     * Uses c_fill which pads/crops to fit the target dimensions without distortion.
     *
     * Example output:
     *   https://res.cloudinary.com/{cloud}/image/upload/c_fill,w_800,h_450/{publicId}
     */
    private String buildCenterCropUrl(String publicId, String resourceType) {
        // Use Cloudinary's URL builder — it automatically appends the cloud name
        return cloudinary.url()
                .resourceType(resourceType)
                .transformation(new com.cloudinary.Transformation()
                        .crop("fill")
                        .width(800)
                        .height(450))
                .generate(publicId);
    }
}
