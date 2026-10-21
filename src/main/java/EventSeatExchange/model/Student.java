package EventSeatExchange.model;

public class Student {
    private int studentId;
    private String studentName;
    private String email;
    private String department;
    private int yearOfStudy;
    private String password = "student123";

    public Student() {
    }

    public Student(int studentId, String studentName, String email, String department, int yearOfStudy) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.password = "student123";
    }

    public Student(int studentId, String studentName, String email, String department, int yearOfStudy, String password) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.password = (password != null && !password.isEmpty()) ? password : "student123";
    }

    public Student(String studentName, String email, String department, int yearOfStudy) {
        this.studentName = studentName;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.password = "student123";
    }

    public Student(String studentName, String email, String department, int yearOfStudy, String password) {
        this.studentName = studentName;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.password = (password != null && !password.isEmpty()) ? password : "student123";
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", yearOfStudy=" + yearOfStudy +
                '}';
    }
}
