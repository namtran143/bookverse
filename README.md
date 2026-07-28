# BookVerse - Bài 8

BookVerse là hệ thống Web API + Web Application quản lý sách điện tử, xây dựng bằng Java 17 và Spring Boot 3.

## Chức năng

### Quản lý sách

- Thêm, xem, sửa, xóa sách.
- Các trường: `id`, `title`, `author`, `isbn`, `year`, `category`, `rating`, `description`, `coverPath`.
- Validation dữ liệu đầu vào và xử lý lỗi JSON thống nhất.
- ISBN không được trùng.

### Ảnh bìa

- Nhận JPG, PNG và WebP.
- Tự động chuyển sang WebP.
- Tạo ba kích thước:
  - `thumbnail`: 200 px.
  - `medium`: 500 px.
  - `large`: 1200 px.
- Lưu theo cấu trúc `uploads/covers/yyyy/MM/id-size.webp`.
- Xóa ảnh cũ khi thay ảnh hoặc xóa sách.
- Hỗ trợ upload nhiều ảnh trong một request.

### Danh sách và tìm kiếm

- Phân trang.
- Lọc theo thể loại và năm xuất bản.
- Sắp xếp theo tên, năm hoặc rating.
- Tìm theo tên sách hoặc tác giả.

### Bulk import

- Import sách từ CSV hoặc XLSX.
- Trả số dòng thành công, thất bại và nội dung lỗi từng dòng.
- CSV mẫu tại `src/main/resources/static/samples/books-sample.csv`.

### Giao diện web

- Trang chủ BookVerse dạng kho sách.
- Danh sách sách dạng card.
- Tìm kiếm, lọc, sắp xếp và phân trang.
- Thêm, sửa, xóa và upload bìa ngay trên giao diện.
- Import CSV/Excel.

## Công nghệ và kiến trúc

- Java 17.
- Spring Boot 3.
- Spring Web.
- Spring Data JPA.
- PostgreSQL khi chạy Docker, H2 khi chạy local.
- Layered Architecture: Controller → Service → Repository.
- DTO + MapStruct.
- Bean Validation.
- Spring Cache.
- Springdoc OpenAPI / Swagger.
- Apache POI.
- WebP ImageIO.
- JUnit 5 + Mockito.
- Docker và Docker Compose.

## Cấu trúc chính

```text
src/main/java/com/ptit/bookverse
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
└── service
```

## Chạy local bằng H2

Yêu cầu: Java 17+ và Maven 3.9+.

```bash
mvn clean package
java -jar target/bookverse-1.0.0.jar
```

Mở:

- Giao diện: `http://localhost:8080/`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`

Thông tin H2:

```text
JDBC URL: jdbc:h2:file:./data/bookverse
User Name: sa
Password: để trống
```

## Chạy Unit Test

```bash
mvn test
```

Các test chính:

- Lấy sách theo ID.
- Không tìm thấy sách.
- Chặn ISBN trùng.
- Tạo sách không có ảnh.
- Chặn cập nhật ISBN trùng.
- Xóa sách và ảnh bìa.
- Import nhiều dòng CSV.

## Chạy bằng Docker + PostgreSQL

Yêu cầu: Docker Desktop đang chạy.

```bash
docker compose up --build
```

Sau khi hai container khởi động:

- Giao diện: `http://localhost:8080/`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

Dừng hệ thống:

```bash
docker compose down
```

Dừng và xóa cả dữ liệu PostgreSQL cùng ảnh đã upload:

```bash
docker compose down -v
```

Docker Compose tạo:

- `bookverse-app`: Spring Boot chạy Java 17.
- `bookverse-postgres`: PostgreSQL 16.
- Volume lưu database.
- Volume lưu ảnh bìa.

## API chính

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/books?page=&size=&sort=&category=&year=` | Danh sách sách |
| GET | `/api/books/{id}` | Chi tiết sách |
| POST | `/api/books` | Tạo sách và upload bìa |
| PUT | `/api/books/{id}` | Cập nhật sách và bìa |
| DELETE | `/api/books/{id}` | Xóa sách |
| GET | `/api/books/search?q=&category=` | Tìm kiếm |
| GET | `/api/books/{id}/cover?size=large` | Lấy ảnh bìa |
| POST | `/api/books/import` | Import CSV/XLSX |
| POST | `/api/books/covers/bulk` | Upload nhiều ảnh |

## Tạo hoặc cập nhật sách

Endpoint dùng `multipart/form-data`:

- `book`: chuỗi JSON.
- `cover`: file ảnh, không bắt buộc.

Ví dụ phần `book`:

```json
{
  "title": "Spring in Action",
  "author": "Craig Walls",
  "isbn": "9781617297571",
  "year": 2022,
  "category": "Programming",
  "rating": 4.6,
  "description": "A practical guide to Spring Framework and Spring Boot."
}
```

## Upload nhiều ảnh

`POST /api/books/covers/bulk` dưới dạng multipart:

- `bookIds`: danh sách ID theo đúng thứ tự file.
- `covers`: danh sách ảnh tương ứng.

Số ID phải bằng số ảnh. Ví dụ ảnh thứ nhất được gắn cho ID thứ nhất.

## Import CSV/XLSX

Thứ tự cột:

```text
title,author,isbn,year,category,rating,description
```

Ví dụ:

```csv
title,author,isbn,year,category,rating,description
Refactoring,Martin Fowler,9780201485677,1999,Programming,4.7,Improving the design of existing code
```

## Cấu hình bằng biến môi trường

| Biến | Mặc định |
|---|---|
| `SERVER_PORT` | `8080` |
| `SPRING_DATASOURCE_URL` | H2 file local |
| `SPRING_DATASOURCE_USERNAME` | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | trống |
| `SPRING_DATASOURCE_DRIVER` | `org.h2.Driver` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
| `BOOKVERSE_H2_CONSOLE` | `true` |
| `BOOKVERSE_UPLOAD_ROOT` | `uploads/covers` |

## Dữ liệu mẫu

Khi database đang trống, ứng dụng tự tạo:

- Clean Code.
- Effective Java.
- The Pragmatic Programmer.

## Kết quả đã kiểm tra

- `mvn clean package` chạy thành công.
- Unit Test chạy thành công trong quá trình build.
- Ứng dụng chạy local bằng H2.
- CRUD hoạt động đầy đủ.
- Tìm kiếm, lọc, sắp xếp và phân trang hoạt động.
- Upload ảnh bìa hoạt động.
- Ảnh được chuyển sang WebP và tạo đủ ba kích thước.
- API trả ảnh bìa hoạt động với `Content-Type: image/webp`.
- Giao diện web hoạt động.
- Import CSV hoạt động.
- Docker image build thành công.
- `bookverse-app` chạy thành công.
- `bookverse-postgres` chạy ở trạng thái `healthy`.
- Ứng dụng kết nối PostgreSQL thành công qua Docker Compose.
- `docker compose down` dừng và xóa container/network thành công.

## Chức năng chưa kiểm tra thủ công riêng

Các chức năng dưới đây đã có code nhưng chưa được kiểm tra thủ công riêng trong phiên demo cuối:

- Import XLSX.
- Upload nhiều ảnh bằng `POST /api/books/covers/bulk`.
