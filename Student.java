/**
 * Model class: holds the data of one registered student.
 */
public class Student {
    private final String name;
    private final String admissionNo;
    private final String course;
    private final String gender;

    public Student(String name, String admissionNo, String course, String gender) {
        this.name = name;
        this.admissionNo = admissionNo;
        this.course = course;
        this.gender = gender;
    }

    public String getName()        { return name; }
    public String getAdmissionNo() { return admissionNo; }
    public String getCourse()      { return course; }
    public String getGender()      { return gender; }

    @Override
    public String toString() {
        return "Name: " + name + " | Adm No: " + admissionNo
                + " | Course: " + course + " | Gender: " + gender;
    }
}
