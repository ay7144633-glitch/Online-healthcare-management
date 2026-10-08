from pathlib import Path
from PIL import Image
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.slide import MSO_SLIDE_LAYOUT
from pptx.enum.shapes import MSO_SHAPE

ROOT = Path(__file__).resolve().parent
SLIDES = ROOT / "rendered-slides"
PDF_PATH = ROOT / "presentation.pdf"
PPTX_PATH = ROOT / "presentation.pptx"
W, H = 13.333, 7.5


def pdf_escape(text: str) -> str:
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def add_pdf_page(pdf: list[str], image: Image.Image) -> None:
    image = image.convert("RGB")
    image_path = ROOT / "rendered-slides" / "_pdf-frame.jpg"
    image.save(image_path, "JPEG", quality=95)
    width, height = image.size
    page_width = 612
    page_height = 792
    scale = min(page_width / width, page_height / height)
    draw_width = width * scale
    draw_height = height * scale
    x = (page_width - draw_width) / 2
    y = (page_height - draw_height) / 2
    content = f"q\n{draw_width:.2f} 0 0 {draw_height:.2f} {x:.2f} {y:.2f} cm\n/Im0 Do\nQ\n"
    pdf.append(content)
    image_path.unlink(missing_ok=True)


def build_pdf() -> None:
    objects: list[str] = []
    page_objects: list[int] = []
    image_objects: list[int] = []
    page_width = 612
    page_height = 792
    page_content = "q\n0 0 612 792 re f\nQ\n"
    for index in range(1, 15):
        image = Image.open(SLIDES / f"slide-{index:02d}.png").convert("RGB")
        image_bytes = __import__("io").BytesIO()
        image.save(image_bytes, "JPEG", quality=95)
        raw = image_bytes.getvalue()
        image_object = len(objects) + 1
        image_objects.append(image_object)
        objects.append(f"<< /Type /XObject /Subtype /Image /Width {image.width} /Height {image.height} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length {len(raw)} >>\nstream\n".encode("latin1") + raw + b"\nendstream")
        page_object = len(objects) + 1
        page_objects.append(page_object)
        objects.append(f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 {page_width} {page_height}] /Resources << /XObject << /Im{image_object} {image_object} 0 R >> >> /Contents {len(objects) + 1} 0 R >>".encode())
        objects.append(f"<< /Length {len(page_content.encode())} >>\nstream\n{page_content}\nendstream".encode())

    # The image is placed a second time in each page content stream below.
    for index, (page_object, image_object) in enumerate(zip(page_objects, image_objects)):
        image = Image.open(SLIDES / f"slide-{index + 1:02d}.png").convert("RGB")
        scale = min(page_width / image.width, page_height / image.height)
        draw_width = image.width * scale
        draw_height = image.height * scale
        x = (page_width - draw_width) / 2
        y = (page_height - draw_height) / 2
        content = f"q\n{draw_width:.2f} 0 0 {draw_height:.2f} {x:.2f} {y:.2f} cm\n/Im{image_object} Do\nQ\n"
        objects[page_object - 1] = objects[page_object - 1].replace(
            f"/Contents {len(objects) + 1} 0 R", f"/Contents {len(objects) + 1} 0 R"
        )
        objects[page_object] = f"<< /Length {len(content.encode())} >>\nstream\n{content}endstream".encode()

    pages = " ".join(f"{n} 0 R" for n in page_objects)
    catalog = len(objects) + 1
    pages_object = len(objects) + 2
    objects.append(f"<< /Type /Catalog /Pages {pages_object} 0 R >>".encode())
    objects.append(f"<< /Type /Pages /Kids [{pages}] /Count {len(page_objects)} >>".encode())

    # Correct object numbering is rebuilt below because page content objects are appended in the original order.
    rebuilt: list[bytes] = []
    for obj in objects:
        rebuilt.append(obj if isinstance(obj, bytes) else str(obj).encode())
    # Rebuild the PDF with explicit object references using a compact writer.
    output = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = []
    for number, obj in enumerate(rebuilt, start=1):
        offsets.append(len(output))
        output.extend(f"{number} 0 obj\n".encode())
        output.extend(obj)
        output.extend(b"\nendobj\n")
    xref = len(output)
    output.extend(f"xref\n0 {len(rebuilt) + 1}\n".encode())
    output.extend(b"0000000000 65535 f \n")
    for offset in offsets:
        output.extend(f"{offset:010d} 00000 n \n".encode())
    output.extend(f"trailer\n<< /Size {len(rebuilt) + 1} /Root {catalog} 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode())
    PDF_PATH.write_bytes(output)


def build_pptx() -> None:
    presentation = Presentation()
    presentation.slide_width = Inches(W)
    presentation.slide_height = Inches(H)
    blank = presentation.slide_layouts[6]
    for index in range(1, 15):
        slide = presentation.slides.add_slide(blank)
        image = Image.open(SLIDES / f"slide-{index:02d}.png").convert("RGB")
        frame = slide.shapes.add_picture(str(SLIDES / f"slide-{index:02d}.png"), Inches(0), Inches(0), Inches(W), Inches(H))
        frame.image.blob
        slide.background.fill.solid()
        slide.background.fill.fore_color.rgb = __import__("pptx.dml.color", fromlist=["RGBColor"]).RGBColor(255, 255, 255)
    presentation.save(PPTX_PATH)


if __name__ == "__main__":
    build_pdf()
    build_pptx()
    print(f"Created {PDF_PATH.name} and {PPTX_PATH.name}")
