package id.ac.ui.cs.advprog.groupproject.catalog.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
public class CatalogImageService {

    private final Cloudinary cloudinary;

    public CatalogImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadCatalogImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must be an image");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "catalog")
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload failed");
            }
            return secureUrl.toString();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload image", exception);
        }
    }
}