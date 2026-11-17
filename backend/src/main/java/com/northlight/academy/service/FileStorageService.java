package com.northlight.academy.service;
import com.northlight.academy.exception.ApiExceptions.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
@Service
public class FileStorageService {
  private static final long MAX = 2L * 1024 * 1024;
  private final Path root;
  public FileStorageService(@Value("${app.storage.dir}") String dir) throws IOException {
    root = Paths.get(dir).toAbsolutePath().normalize(); Files.createDirectories(root);
  }
  /** Validates size and real content (magic bytes, not the client-supplied type) and stores under a random name. */
  public String storeImage(MultipartFile f){
    if (f == null || f.isEmpty()) throw new BadRequestException("No file uploaded");
    if (f.getSize() > MAX) throw new BadRequestException("File is too large (max 2 MB)");
    try {
      byte[] data = f.getBytes();
      String ext = detect(data);
      if (ext == null) throw new BadRequestException("Only JPEG, PNG or WebP images are allowed");
      String name = UUID.randomUUID() + "." + ext;
      Path target = root.resolve(name).normalize();
      if (!target.startsWith(root)) throw new BadRequestException("Invalid file name");
      Files.write(target, data, StandardOpenOption.CREATE_NEW);
      return name;
    } catch (IOException e) { throw new BadRequestException("Could not store file"); }
  }
  public void delete(String name){
    if (name == null) return;
    try { Path p = root.resolve(name).normalize(); if (p.startsWith(root)) Files.deleteIfExists(p); } catch (IOException ignored) {}
  }
  private static String detect(byte[] d){
    if (d.length > 3 && (d[0]&0xFF)==0xFF && (d[1]&0xFF)==0xD8 && (d[2]&0xFF)==0xFF) return "jpg";
    if (d.length > 8 && (d[0]&0xFF)==0x89 && d[1]=='P' && d[2]=='N' && d[3]=='G') return "png";
    if (d.length > 12 && d[0]=='R' && d[1]=='I' && d[2]=='F' && d[3]=='F' && d[8]=='W' && d[9]=='E' && d[10]=='B' && d[11]=='P') return "webp";
    return null;
  }
}
