package id.ac.ui.cs.advprog.groupproject.catalog.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogImageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    private CatalogImageService catalogImageService;

    @BeforeEach
    void setUp() {
        catalogImageService = new CatalogImageService(cloudinary);
    }

    @Test
    void testUploadCatalogImageReturnsNullWhenFileIsNull() {
        String imageUrl = catalogImageService.uploadCatalogImage(null);

        assertNull(imageUrl);
        verify(cloudinary, never()).uploader();
    }

    @Test
    void testUploadCatalogImageReturnsNullWhenFileIsEmpty() {
        when(multipartFile.isEmpty()).thenReturn(true);

        String imageUrl = catalogImageService.uploadCatalogImage(multipartFile);

        assertNull(imageUrl);
        verify(cloudinary, never()).uploader();
    }

    @Test
    void testUploadCatalogImageThrowsBadRequestWhenContentTypeIsNotImage() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> catalogImageService.uploadCatalogImage(multipartFile));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Uploaded file must be an image", exception.getReason());
        verify(cloudinary, never()).uploader();
    }

    @Test
    void testUploadCatalogImageReturnsSecureUrlWhenUploadSucceeds() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getBytes()).thenReturn(new byte[] {1, 2, 3});

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/catalog/image.png");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), ArgumentMatchers.<String, Object>anyMap())).thenReturn(uploadResult);

        String imageUrl = catalogImageService.uploadCatalogImage(multipartFile);

        assertEquals("https://res.cloudinary.com/demo/image/upload/catalog/image.png", imageUrl);
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(any(byte[].class), ArgumentMatchers.<String, Object>anyMap());
    }

    @Test
    void testUploadCatalogImageThrowsBadGatewayWhenSecureUrlIsMissing() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenReturn(new byte[] {4, 5, 6});

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), ArgumentMatchers.<String, Object>anyMap())).thenReturn(new HashMap<>());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> catalogImageService.uploadCatalogImage(multipartFile));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Cloudinary upload failed", exception.getReason());
    }

    @Test
    void testUploadCatalogImageThrowsBadGatewayWhenFileReadFails() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/webp");
        when(multipartFile.getBytes()).thenThrow(new IOException("I/O issue"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> catalogImageService.uploadCatalogImage(multipartFile));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Failed to upload image", exception.getReason());
        assertNotNull(exception.getCause());
        assertEquals(IOException.class, exception.getCause().getClass());
    }
}
