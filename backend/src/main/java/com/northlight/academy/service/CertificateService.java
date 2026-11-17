package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.repository.*;
import com.northlight.academy.util.Hashing;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
public class CertificateService {
  private final CertificateRepository certs; private final EnrollmentRepository enrollments;
  public CertificateService(CertificateRepository certs, EnrollmentRepository enrollments){ this.certs=certs; this.enrollments=enrollments; }

  /** Issues a certificate only for a COMPLETED enrollment. Idempotent. */
  @Transactional
  public Certificate issue(Enrollment e){
    if (e.getStatus() != EnrollmentStatus.COMPLETED) throw new BadRequestException("Course is not completed");
    return certs.findByEnrollmentId(e.getId()).orElseGet(() -> {
      Certificate c = new Certificate(); c.setEnrollment(e); c.setCertificateCode(Hashing.certificateCode()); return certs.save(c);
    });
  }
  private CertificateDto dto(Certificate c){ return new CertificateDto(c.getId(), c.getCertificateCode(), c.getEnrollment().getCourse().getTitle(), c.getEnrollment().getStudent().getFullName(), c.getIssuedAt()); }
  @Transactional(readOnly = true)
  public List<CertificateDto> forStudent(Long studentId){ return certs.findByEnrollmentStudentIdOrderByIssuedAtDesc(studentId).stream().map(this::dto).toList(); }
  @Transactional(readOnly = true)
  public VerifyDto verify(String code){
    return certs.findByCertificateCode(code.trim().toUpperCase())
      .map(c -> new VerifyDto(true, c.getCertificateCode(), c.getEnrollment().getStudent().getFullName(), c.getEnrollment().getCourse().getTitle(), c.getIssuedAt()))
      .orElse(new VerifyDto(false, code, null, null, null));
  }
  /** Ownership enforced: the certificate must belong to the authenticated student. Unknown and foreign codes both yield 404. */
  @Transactional(readOnly = true)
  public byte[] pdf(Long studentId, String code){
    Certificate c = certs.findByCertificateCode(code).filter(x -> x.getEnrollment().getStudent().getId().equals(studentId)).orElseThrow(() -> new NotFoundException("Certificate not found"));
    return render(c);
  }
  private byte[] render(Certificate c){
    Color navy = new Color(0x14, 0x2B, 0x4A), blue = new Color(0x28, 0x57, 0xA4), grey = new Color(0x66, 0x75, 0x8A);
    Rectangle page = PageSize.A4.rotate();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document doc = new Document(page, 60, 60, 60, 60);
    try {
      PdfWriter w = PdfWriter.getInstance(doc, out); doc.open();
      PdfContentByte cb = w.getDirectContent();
      cb.setColorStroke(navy); cb.setLineWidth(3f); cb.rectangle(24, 24, page.getWidth() - 48, page.getHeight() - 48); cb.stroke();
      cb.setColorStroke(blue); cb.setLineWidth(0.8f); cb.rectangle(32, 32, page.getWidth() - 64, page.getHeight() - 64); cb.stroke();
      doc.add(line("NORTHLIGHT ACADEMY", FontFactory.HELVETICA_BOLD, 16, blue, 20, 40));
      doc.add(line("Certificate of Completion", FontFactory.HELVETICA_BOLD, 36, navy, 10, 30));
      doc.add(line("This is to certify that", FontFactory.HELVETICA, 14, grey, 10, 10));
      doc.add(line(c.getEnrollment().getStudent().getFullName(), FontFactory.HELVETICA_BOLD, 30, navy, 10, 10));
      doc.add(line("has successfully completed the course", FontFactory.HELVETICA, 14, grey, 10, 10));
      doc.add(line(c.getEnrollment().getCourse().getTitle(), FontFactory.HELVETICA_BOLD, 22, blue, 10, 24));
      String date = DateTimeFormatter.ofPattern("d MMMM yyyy").format(c.getIssuedAt().atZone(ZoneOffset.UTC));
      doc.add(line("Completed on " + date, FontFactory.HELVETICA, 13, navy, 10, 6));
      doc.add(line("Certificate ID: " + c.getCertificateCode(), FontFactory.HELVETICA_BOLD, 12, navy, 4, 4));
      doc.add(line("Verify at /verify/" + c.getCertificateCode(), FontFactory.HELVETICA, 10, grey, 4, 0));
      doc.close();
    } catch (DocumentException e){ throw new IllegalStateException("Could not generate certificate", e); }
    return out.toByteArray();
  }
  private static Paragraph line(String text, String font, float size, Color color, float before, float after){
    Paragraph p = new Paragraph(text, FontFactory.getFont(font, size, Font.NORMAL, color));
    p.setAlignment(Element.ALIGN_CENTER); p.setSpacingBefore(before); p.setSpacingAfter(after); return p;
  }
}
