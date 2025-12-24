# Giải Thích Chi Tiết Kiến Trúc Hệ Thống TDTU Mobile Banking

Tài liệu này giải thích chi tiết về cấu trúc và kiến trúc của hệ thống TDTU Mobile Banking, bao gồm Class Diagram (Sơ đồ lớp) và ERD (Entity Relationship Diagram - Sơ đồ quan hệ thực thể).

---

## Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Class Diagram - Sơ Đồ Lớp](#class-diagram---sơ-đồ-lớp)
3. [ERD - Sơ Đồ Quan Hệ Thực Thể](#erd---sơ-đồ-quan-hệ-thực-thể)
4. [Mối Quan Hệ Giữa Class Diagram và ERD](#mối-quan-hệ-giữa-class-diagram-và-erd)

---

## Tổng Quan

Hệ thống TDTU Mobile Banking được thiết kế theo kiến trúc phân lớp (Layered Architecture) với các thành phần chính:

- **Domain Layer**: Các lớp thực thể (Entity) và business logic
- **Data Layer**: Repository pattern để quản lý dữ liệu
- **Use Case Layer**: Các use case xử lý nghiệp vụ
- **Service Layer**: Các dịch vụ hỗ trợ (Payment, OTP)
- **Presentation Layer**: Giao diện người dùng

---

## Class Diagram - Sơ Đồ Lớp

Class Diagram mô tả cấu trúc các lớp trong hệ thống, mối quan hệ giữa chúng, và cách chúng tương tác với nhau.

### 1. Các Enumeration (Kiểu Liệt Kê)

Hệ thống sử dụng các enumeration để định nghĩa các trạng thái và loại dữ liệu:

#### UserRole (Vai Trò Người Dùng)
- `CUSTOMER`: Khách hàng
- `OFFICER`: Nhân viên ngân hàng

#### AccountType (Loại Tài Khoản)
- `CHECKING`: Tài khoản thanh toán
- `SAVING`: Tài khoản tiết kiệm
- `MORTGAGE`: Tài khoản thế chấp

#### TransactionStatus (Trạng Thái Giao Dịch)
- `SUCCESS`: Thành công
- `FAILED`: Thất bại
- `PENDING`: Đang chờ xử lý

#### TransactionType (Loại Giao Dịch)
- `TRANSFER_INTERNAL`: Chuyển khoản nội bộ
- `TRANSFER_EXTERNAL`: Chuyển khoản liên ngân hàng
- `BILL_PAYMENT`: Thanh toán hóa đơn
- `DEPOSIT`: Nạp tiền
- `WITHDRAWAL`: Rút tiền

#### KycStatus (Trạng Thái KYC)
- `VERIFIED`: Đã xác thực
- `PENDING`: Đang chờ xác thực
- `NONE`: Chưa có

#### BillStatus (Trạng Thái Hóa Đơn)
- `UNPAID`: Chưa thanh toán
- `PAID`: Đã thanh toán
- `OVERDUE`: Quá hạn
- `CANCELLED`: Đã hủy

### 2. Các Lớp Core (Lớp Cốt Lõi)

#### User (Lớp Người Dùng - Abstract)

Lớp trừu tượng đại diện cho người dùng trong hệ thống. Đây là lớp cha cho `Customer` và `BankOfficer`.

**Thuộc tính:**
- `uid` (String): Định danh duy nhất của người dùng
- `email` (String): Email đăng nhập
- `fullName` (String): Họ và tên đầy đủ
- `phoneNumber` (String): Số điện thoại
- `role` (UserRole): Vai trò (CUSTOMER hoặc OFFICER)
- `kycStatus` (KycStatus): Trạng thái xác thực danh tính
- `avatarUrl` (String): Đường dẫn ảnh đại diện

**Mối quan hệ:**
- Được kế thừa bởi `Customer` và `BankOfficer` (Inheritance)
- Sử dụng `UserRole` và `KycStatus` (Dependency)

#### Customer (Khách Hàng)

Lớp đại diện cho khách hàng của ngân hàng, kế thừa từ `User`.

**Thuộc tính:**
- `deviceId` (String): Định danh thiết bị di động

**Phương thức:**
- `uploadKYCImage()`: Tải lên ảnh giấy tờ tùy thân để xác thực KYC
- `transferMoney()`: Chuyển tiền giữa các tài khoản
- `payBill()`: Thanh toán hóa đơn

**Mối quan hệ:**
- Kế thừa từ `User` (Inheritance)
- Sở hữu nhiều `BankAccount` (Composition: 1 Customer → 0..* BankAccount)
- Thanh toán nhiều `Bill` (Association: 1 Customer → 0..* Bill)
- Khởi tạo `Transaction` (Dependency)
- Sử dụng `PaymentService` và `OtpService` (Dependency)

#### BankOfficer (Nhân Viên Ngân Hàng)

Lớp đại diện cho nhân viên ngân hàng, kế thừa từ `User`.

**Thuộc tính:**
- `employeeId` (String): Mã nhân viên
- `department` (String): Phòng ban

**Phương thức:**
- `verifyKYC()`: Xác thực KYC cho khách hàng
- `createAccount()`: Tạo tài khoản mới cho khách hàng
- `updateInterestRate()`: Cập nhật lãi suất cho tài khoản tiết kiệm

**Mối quan hệ:**
- Kế thừa từ `User` (Inheritance)
- Quản lý nhiều `Customer` (Association: 1 BankOfficer → 0..* Customer)
- Tạo `BankAccount` (Association)
- Thao tác trên `BankAccount` (Dependency)

#### BankAccount (Tài Khoản Ngân Hàng - Abstract)

Lớp trừu tượng đại diện cho tài khoản ngân hàng. Đây là lớp cha cho các loại tài khoản cụ thể.

**Thuộc tính:**
- `accountId` (String): Định danh duy nhất của tài khoản
- `ownerId` (String): ID của chủ tài khoản (Customer)
- `balance` (Double): Số dư hiện tại
- `accountType` (AccountType): Loại tài khoản
- `currency` (String): Đơn vị tiền tệ
- `interestRate` (Double): Lãi suất (cho tài khoản tiết kiệm)
- `termMonth` (Int): Kỳ hạn theo tháng (cho tài khoản tiết kiệm)
- `principalAmount` (Double): Số tiền gốc (cho tài khoản thế chấp)
- `mortgageRate` (Double): Lãi suất thế chấp
- `termMonths` (Int): Kỳ hạn thế chấp theo tháng
- `startDate` (Long): Ngày bắt đầu

**Phương thức:**
- `deposit(amount)`: Nạp tiền vào tài khoản
- `withdraw(amount)`: Rút tiền từ tài khoản

**Mối quan hệ:**
- Được kế thừa bởi `CheckingAccount`, `SavingAccount`, `MortgageAccount` (Inheritance)
- Sử dụng `AccountType` (Dependency)
- Có nhiều `Transaction` (Composition: 1 BankAccount → 0..* Transaction)

#### CheckingAccount (Tài Khoản Thanh Toán)

Tài khoản thanh toán thông thường, kế thừa từ `BankAccount`.

**Thuộc tính:**
- `overdraftLimit` (Double): Hạn mức thấu chi (cho phép số dư âm đến một giới hạn)

**Đặc điểm:**
- Cho phép rút tiền và thanh toán hàng ngày
- Có thể có hạn mức thấu chi

#### SavingAccount (Tài Khoản Tiết Kiệm)

Tài khoản tiết kiệm có lãi suất, kế thừa từ `BankAccount`.

**Phương thức:**
- `calculateMonthlyProfit()`: Tính lợi nhuận hàng tháng dựa trên lãi suất và số dư

**Đặc điểm:**
- Có lãi suất và kỳ hạn
- Tính lãi theo tháng

#### MortgageAccount (Tài Khoản Thế Chấp)

Tài khoản thế chấp, kế thừa từ `BankAccount`.

**Phương thức:**
- `calculateMonthlyPayment()`: Tính số tiền phải trả hàng tháng dựa trên số tiền gốc, lãi suất và kỳ hạn

**Đặc điểm:**
- Có số tiền gốc, lãi suất thế chấp và kỳ hạn
- Tính toán số tiền trả hàng tháng

#### Transaction (Giao Dịch)

Lớp đại diện cho một giao dịch trong hệ thống.

**Thuộc tính:**
- `transactionId` (String): Định danh duy nhất của giao dịch
- `senderAccountId` (String): ID tài khoản người gửi
- `receiverAccountId` (String): ID tài khoản người nhận
- `amount` (Double): Số tiền giao dịch
- `type` (TransactionType): Loại giao dịch
- `status` (TransactionStatus): Trạng thái giao dịch
- `timestamp` (Long): Thời gian thực hiện
- `description` (String): Mô tả giao dịch

**Mối quan hệ:**
- Thuộc về `BankAccount` (Composition)
- Sử dụng `TransactionType` và `TransactionStatus` (Dependency)
- Được khởi tạo bởi `Customer` (Dependency)

#### Bill (Hóa Đơn)

Lớp đại diện cho hóa đơn tiện ích (điện, nước, internet, v.v.).

**Thuộc tính:**
- `billId` (String): Định danh duy nhất của hóa đơn
- `billCode` (String): Mã hóa đơn
- `billType` (String): Loại hóa đơn (điện, nước, v.v.)
- `customerName` (String): Tên khách hàng
- `customerCode` (String): Mã khách hàng
- `provider` (String): Nhà cung cấp dịch vụ
- `amount` (Double): Số tiền cần thanh toán
- `status` (BillStatus): Trạng thái hóa đơn
- `dueDate` (Long): Ngày đến hạn thanh toán
- `createdAt` (Long): Ngày tạo hóa đơn
- `paidAt` (Long): Ngày thanh toán (nullable)
- `description` (String): Mô tả hóa đơn

**Mối quan hệ:**
- Được thanh toán bởi `Customer` (Association: 1 Customer → 0..* Bill)
- Sử dụng `BillStatus` (Dependency)

#### Branch (Chi Nhánh Ngân Hàng)

Lớp đại diện cho chi nhánh ngân hàng.

**Thuộc tính:**
- `branchId` (String): Định danh duy nhất của chi nhánh
- `name` (String): Tên chi nhánh
- `address` (String): Địa chỉ
- `latitude` (Double): Vĩ độ (cho bản đồ)
- `longitude` (Double): Kinh độ (cho bản đồ)

**Mối quan hệ:**
- Sử dụng `UseCases` (Dependency)
- Sử dụng `PaymentService` (Dependency)
- Phụ thuộc vào `IRepository` (Dependency)

### 3. Repository Layer (Lớp Kho Dữ Liệu)

#### IRepository (Interface Repository)

Interface định nghĩa các repository để truy cập dữ liệu.

**Các Repository:**
- `AccountRepository`: Quản lý dữ liệu tài khoản
- `TransactionRepository`: Quản lý dữ liệu giao dịch
- `BillRepository`: Quản lý dữ liệu hóa đơn
- `AuthRepository`: Quản lý xác thực
- `UserRepository`: Quản lý dữ liệu người dùng
- `BranchRepository`: Quản lý dữ liệu chi nhánh
- `BankRepository`: Quản lý dữ liệu ngân hàng

**Mối quan hệ:**
- Được sử dụng bởi `UseCases` (Dependency)

### 4. Use Case Layer (Lớp Use Case)

#### UseCases (Package)

Package chứa các use case xử lý nghiệp vụ.

**Các Use Case:**
- `AccountUseCases`: Xử lý nghiệp vụ tài khoản
- `TransactionUseCases`: Xử lý nghiệp vụ giao dịch
- `BillUseCases`: Xử lý nghiệp vụ hóa đơn
- `AuthUseCases`: Xử lý xác thực
- `UserUseCases`: Xử lý nghiệp vụ người dùng
- `BranchUseCases`: Xử lý nghiệp vụ chi nhánh
- `BankUseCases`: Xử lý nghiệp vụ ngân hàng
- `UtilitiesUseCases`: Xử lý các tiện ích

**Mối quan hệ:**
- Phụ thuộc vào `IRepository` (Dependency)
- Thao tác trên `BankAccount`, `Transaction`, `Bill`, `User` (Dependency)
- Sử dụng `PaymentService` và `OtpService` (Dependency)

### 5. Service Layer (Lớp Dịch Vụ)

#### PaymentService (Dịch Vụ Thanh Toán)

Dịch vụ xử lý thanh toán.

**Phương thức:**
- `createPaymentIntent()`: Tạo intent thanh toán (tích hợp với Stripe)
- `confirmPayment()`: Xác nhận thanh toán

**Mối quan hệ:**
- Được sử dụng bởi `UseCases` (Dependency)
- Thao tác trên `Customer` (Dependency)

#### OtpService (Dịch Vụ OTP)

Dịch vụ xử lý mã OTP (One-Time Password) để xác thực giao dịch.

**Phương thức:**
- `generateOtp()`: Tạo mã OTP
- `validateOtp()`: Xác thực mã OTP

**Mối quan hệ:**
- Được sử dụng bởi `UseCases` (Dependency)
- Thao tác trên `Customer` (Dependency)

### 6. Các Mối Quan Hệ Trong Class Diagram

#### Inheritance (Kế Thừa)

- `User` ← `Customer`
- `User` ← `BankOfficer`
- `BankAccount` ← `CheckingAccount`
- `BankAccount` ← `SavingAccount`
- `BankAccount` ← `MortgageAccount`

#### Composition (Thành Phần)

- `Customer` (1) *-- (0..*) `BankAccount`: Một khách hàng sở hữu nhiều tài khoản
- `BankAccount` (1) --> (0..*) `Transaction`: Một tài khoản có nhiều giao dịch

#### Association (Liên Kết)

- `Customer` (1) --> (0..*) `Bill`: Một khách hàng thanh toán nhiều hóa đơn
- `BankOfficer` (1) --> (0..*) `Customer`: Một nhân viên quản lý nhiều khách hàng
- `BankOfficer` --> `BankAccount`: Nhân viên tạo tài khoản

#### Dependency (Phụ Thuộc)

- Các lớp phụ thuộc vào Enum (UserRole, AccountType, v.v.)
- `UseCases` phụ thuộc vào `IRepository`
- `UseCases` sử dụng `PaymentService` và `OtpService`
- `Customer` khởi tạo `Transaction`

---

## ERD - Sơ Đồ Quan Hệ Thực Thể

ERD mô tả cấu trúc cơ sở dữ liệu và các mối quan hệ giữa các bảng (collections trong Firestore).

### 1. Các Bảng (Collections) Trong Database

Hệ thống sử dụng Firebase Firestore (NoSQL) với các collection chính:

#### User Collection (`users`)

Bảng lưu trữ thông tin người dùng.

| Trường | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|--------|--------------|-----------|-------|
| `uid` | `string` | Primary Key | Định danh duy nhất (từ Firebase Auth) |
| `fullName` | `string` | - | Họ và tên đầy đủ |
| `email` | `string` | - | Email đăng nhập |
| `role` | `string` | - | Vai trò: "CUSTOMER" hoặc "OFFICER" |
| `phoneNumber` | `string` | - | Số điện thoại |
| `kycStatus` | `string` | - | Trạng thái KYC: "VERIFIED", "PENDING", "NONE" |
| `avatarUrl` | `string` | - | URL ảnh đại diện |

**Đặc điểm:**
- Mỗi user có một `uid` duy nhất
- `uid` được tạo tự động bởi Firebase Authentication
- `role` xác định quyền truy cập trong hệ thống

#### Account Collection (`accounts`)

Bảng lưu trữ thông tin tài khoản ngân hàng.

| Trường | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|--------|--------------|-----------|-------|
| `accountId` | `string` | Primary Key | Định danh duy nhất của tài khoản |
| `ownerId` | `string` | Foreign Key → User.uid | ID chủ tài khoản |
| `accountType` | `string` | - | Loại: "CHECKING", "SAVING", "MORTGAGE" |
| `balance` | `double` | - | Số dư hiện tại |
| `currency` | `string` | - | Đơn vị tiền tệ (VND, USD, v.v.) |
| `interestRate` | `double` | nullable | Lãi suất (cho SAVING) |
| `termMonth` | `int` | nullable | Kỳ hạn theo tháng (cho SAVING) |
| `principalAmount` | `double` | nullable | Số tiền gốc (cho MORTGAGE) |
| `mortgageRate` | `double` | nullable | Lãi suất thế chấp (cho MORTGAGE) |
| `termMonths` | `int` | nullable | Kỳ hạn thế chấp (cho MORTGAGE) |
| `startDate` | `timestamp` | nullable | Ngày bắt đầu |

**Đặc điểm:**
- Mỗi tài khoản thuộc về một user (`ownerId`)
- Các trường nullable phụ thuộc vào `accountType`:
  - `CHECKING`: Không cần `interestRate`, `termMonth`, `principalAmount`, `mortgageRate`, `termMonths`
  - `SAVING`: Cần `interestRate`, `termMonth`
  - `MORTGAGE`: Cần `principalAmount`, `mortgageRate`, `termMonths`

#### Transaction Collection (`transactions`)

Bảng lưu trữ lịch sử giao dịch.

| Trường | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|--------|--------------|-----------|-------|
| `transactionId` | `string` | Primary Key | Định danh duy nhất của giao dịch |
| `senderAccountId` | `string` | Foreign Key → Account.accountId | ID tài khoản người gửi |
| `receiverAccountId` | `string` | Foreign Key → Account.accountId | ID tài khoản người nhận |
| `amount` | `double` | - | Số tiền giao dịch |
| `type` | `string` | - | Loại: "TRANSFER_INTERNAL", "TRANSFER_EXTERNAL", "BILL_PAYMENT", "DEPOSIT", "WITHDRAWAL" |
| `status` | `string` | - | Trạng thái: "SUCCESS", "FAILED", "PENDING" |
| `timestamp` | `timestamp` | - | Thời gian thực hiện |
| `description` | `string` | - | Mô tả giao dịch |

**Đặc điểm:**
- Mỗi giao dịch liên kết với 2 tài khoản (gửi và nhận)
- `receiverAccountId` có thể null cho giao dịch DEPOSIT/WITHDRAWAL
- `status` theo dõi trạng thái xử lý giao dịch

#### Bill Collection (`bills`)

Bảng lưu trữ hóa đơn tiện ích.

| Trường | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|--------|--------------|-----------|-------|
| `billId` | `string` | Primary Key | Định danh duy nhất của hóa đơn |
| `billCode` | `string` | - | Mã hóa đơn từ nhà cung cấp |
| `billType` | `string` | - | Loại: "ELECTRICITY", "WATER", "INTERNET", v.v. |
| `customerName` | `string` | - | Tên khách hàng |
| `customerCode` | `string` | - | Mã khách hàng từ nhà cung cấp |
| `provider` | `string` | - | Nhà cung cấp dịch vụ |
| `amount` | `double` | - | Số tiền cần thanh toán |
| `status` | `string` | - | Trạng thái: "UNPAID", "PAID", "OVERDUE", "CANCELLED" |
| `dueDate` | `timestamp` | - | Ngày đến hạn thanh toán |
| `createdAt` | `timestamp` | - | Ngày tạo hóa đơn |
| `paidAt` | `timestamp` | nullable | Ngày thanh toán (null nếu chưa thanh toán) |
| `description` | `string` | - | Mô tả hóa đơn |

**Đặc điểm:**
- Hóa đơn không liên kết trực tiếp với User trong database (liên kết qua `customerCode`)
- `paidAt` chỉ có giá trị khi `status` = "PAID"

#### Branch Collection (`branches`)

Bảng lưu trữ thông tin chi nhánh ngân hàng.

| Trường | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|--------|--------------|-----------|-------|
| `branchId` | `string` | Primary Key | Định danh duy nhất của chi nhánh |
| `name` | `string` | - | Tên chi nhánh |
| `latitude` | `double` | - | Vĩ độ (cho Google Maps) |
| `longitude` | `double` | - | Kinh độ (cho Google Maps) |
| `address` | `string` | - | Địa chỉ chi nhánh |

**Đặc điểm:**
- Sử dụng `latitude` và `longitude` để hiển thị trên bản đồ
- Không có mối quan hệ trực tiếp với các bảng khác

### 2. Các Mối Quan Hệ Trong ERD

#### User → Account (One-to-Many)

**Mối quan hệ:** `User.uid` → `Account.ownerId`

- Một user có thể sở hữu nhiều tài khoản
- Mỗi tài khoản chỉ thuộc về một user
- Ràng buộc: `Account.ownerId` phải tồn tại trong `User.uid`

**Ví dụ:**
```
User (uid: "user123")
  ├── Account (accountId: "acc001", ownerId: "user123", type: "CHECKING")
  ├── Account (accountId: "acc002", ownerId: "user123", type: "SAVING")
  └── Account (accountId: "acc003", ownerId: "user123", type: "MORTGAGE")
```

#### Account → Transaction (One-to-Many)

**Mối quan hệ:** `Account.accountId` → `Transaction.senderAccountId` và `Transaction.receiverAccountId`

- Một tài khoản có thể là người gửi hoặc người nhận trong nhiều giao dịch
- Mỗi giao dịch liên kết với 2 tài khoản (gửi và nhận)
- Ràng buộc: `Transaction.senderAccountId` và `Transaction.receiverAccountId` phải tồn tại trong `Account.accountId`

**Ví dụ:**
```
Account (accountId: "acc001")
  ├── Transaction (senderAccountId: "acc001", receiverAccountId: "acc002", type: "TRANSFER_INTERNAL")
  ├── Transaction (senderAccountId: "acc003", receiverAccountId: "acc001", type: "TRANSFER_INTERNAL")
  └── Transaction (senderAccountId: "acc001", receiverAccountId: null, type: "WITHDRAWAL")
```

**Lưu ý:**
- Giao dịch `DEPOSIT` và `WITHDRAWAL` có thể có `receiverAccountId` = null
- Giao dịch `BILL_PAYMENT` có `receiverAccountId` trỏ đến tài khoản của nhà cung cấp dịch vụ

### 3. Sơ Đồ ERD Tổng Quan

```
┌─────────────┐
│    User     │
│  (users)    │
│─────────────│
│ uid (PK)    │◄──────┐
│ fullName    │       │
│ email       │       │
│ role        │       │
│ phoneNumber │       │
│ kycStatus   │       │
│ avatarUrl   │       │
└─────────────┘       │
                      │
                      │ ownerId (FK)
                      │
┌─────────────┐       │
│   Account   │       │
│ (accounts)  │       │
│─────────────│       │
│ accountId   │◄──────┘
│ ownerId(FK) │◄──────┐
│ accountType │       │
│ balance     │       │
│ currency    │       │
│ ...         │       │
└─────────────┘       │
                      │
        ┌─────────────┴─────────────┐
        │                           │
        │ senderAccountId (FK)      │ receiverAccountId (FK)
        │                           │
┌───────┴──────────────┐            │
│    Transaction       │            │
│  (transactions)      │            │
│──────────────────────│            │
│ transactionId (PK)   │            │
│ senderAccountId (FK) │────────────┘
│ receiverAccountId(FK)│
│ amount               │
│ type                 │
│ status               │
│ timestamp            │
│ description          │
└──────────────────────┘

┌─────────────┐
│    Bill     │
│   (bills)   │
│─────────────│
│ billId (PK) │
│ billCode    │
│ billType    │
│ customerName│
│ customerCode│
│ provider    │
│ amount      │
│ status      │
│ dueDate     │
│ createdAt   │
│ paidAt      │
│ description │
└─────────────┘

┌─────────────┐
│   Branch    │
│  (branches) │
│─────────────│
│ branchId(PK)│
│ name        │
│ latitude    │
│ longitude   │
│ address     │
└─────────────┘
```

---

## Mối Quan Hệ Giữa Class Diagram và ERD

### 1. Mapping Giữa Class và Database

| Class Diagram | ERD Collection | Mô Tả |
|---------------|----------------|-------|
| `User` | `users` | Lớp User được lưu trữ trong collection `users` |
| `Customer` | `users` (với role="CUSTOMER") | Customer là một instance của User với role CUSTOMER |
| `BankOfficer` | `users` (với role="OFFICER") | BankOfficer là một instance của User với role OFFICER |
| `BankAccount` | `accounts` | Lớp BankAccount được lưu trữ trong collection `accounts` |
| `CheckingAccount` | `accounts` (với accountType="CHECKING") | CheckingAccount là một instance của BankAccount |
| `SavingAccount` | `accounts` (với accountType="SAVING") | SavingAccount là một instance của BankAccount |
| `MortgageAccount` | `accounts` (với accountType="MORTGAGE") | MortgageAccount là một instance của BankAccount |
| `Transaction` | `transactions` | Lớp Transaction được lưu trữ trong collection `transactions` |
| `Bill` | `bills` | Lớp Bill được lưu trữ trong collection `bills` |
| `Branch` | `branches` | Lớp Branch được lưu trữ trong collection `branches` |

### 2. Sự Khác Biệt Quan Trọng

#### Class Diagram (OOP - Hướng Đối Tượng)

- **Inheritance (Kế thừa):** `User` là lớp abstract, `Customer` và `BankOfficer` kế thừa từ `User`
- **Polymorphism (Đa hình):** `BankAccount` là lớp abstract, các loại tài khoản kế thừa và override phương thức
- **Methods (Phương thức):** Các lớp có phương thức như `deposit()`, `withdraw()`, `transferMoney()`
- **Composition:** Mối quan hệ "sở hữu" rõ ràng (Customer owns BankAccount)

#### ERD (Database - Cơ Sở Dữ Liệu)

- **NoSQL Structure:** Firestore không hỗ trợ inheritance, sử dụng single table với trường phân biệt
- **Denormalization:** Một số thông tin được lưu trữ trực tiếp (ví dụ: `customerName` trong Bill)
- **Foreign Keys:** Sử dụng `ownerId`, `senderAccountId`, `receiverAccountId` để liên kết
- **No Methods:** Database chỉ lưu trữ dữ liệu, không có logic

### 3. Luồng Dữ Liệu

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│              (UI - Jetpack Compose)                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    Use Case Layer                        │
│  (AccountUseCases, TransactionUseCases, v.v.)           │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│  Repository     │    │   Services       │
│  (IRepository)  │    │  (Payment, OTP)  │
└────────┬────────┘    └─────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                            │
│  (Firestore Collections - ERD)                          │
│  users, accounts, transactions, bills, branches         │
└─────────────────────────────────────────────────────────┘
```

### 4. Ví Dụ Cụ Thể: Chuyển Tiền

**Class Diagram Perspective:**
1. `Customer` gọi phương thức `transferMoney()`
2. `Customer` tạo một `Transaction` mới
3. `BankAccount` gọi `withdraw()` trên tài khoản gửi
4. `BankAccount` gọi `deposit()` trên tài khoản nhận
5. `OtpService` được sử dụng để xác thực

**ERD Perspective:**
1. Tạo document mới trong collection `transactions`
2. Cập nhật `balance` trong document `accounts` của tài khoản gửi
3. Cập nhật `balance` trong document `accounts` của tài khoản nhận
4. Liên kết qua `senderAccountId` và `receiverAccountId`

**Implementation:**
- Use Case (`TransactionUseCases`) xử lý logic nghiệp vụ
- Repository (`TransactionRepository`) thực hiện CRUD operations
- Firestore lưu trữ dữ liệu theo cấu trúc ERD

---

## Tổng Kết

### Class Diagram
- Mô tả cấu trúc OOP của hệ thống
- Định nghĩa các lớp, phương thức, và mối quan hệ
- Hỗ trợ inheritance, polymorphism, và encapsulation
- Tập trung vào business logic và behavior

### ERD
- Mô tả cấu trúc database (Firestore collections)
- Định nghĩa các bảng, trường, và foreign keys
- Tập trung vào data storage và relationships
- Tối ưu cho truy vấn và lưu trữ

### Kết Hợp
- Class Diagram và ERD bổ sung cho nhau
- Use Cases kết nối giữa Domain Model (Class Diagram) và Data Model (ERD)
- Repository Pattern đóng vai trò adapter giữa OOP và Database

Hệ thống TDTU Mobile Banking được thiết kế với kiến trúc rõ ràng, tách biệt các lớp, dễ bảo trì và mở rộng.

