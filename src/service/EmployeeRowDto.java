package service;

public class EmployeeRowDto {

    private final String employeeId;
    private final String lastName;
    private final String firstName;
    private final String sssNumber;
    private final String philHealthNumber;
    private final String tinNumber;
    private final String pagIbigNumber;

    public EmployeeRowDto(
            String employeeId,
            String lastName,
            String firstName,
            String sssNumber,
            String philHealthNumber,
            String tinNumber,
            String pagIbigNumber
    ) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.sssNumber = sssNumber;
        this.philHealthNumber = philHealthNumber;
        this.tinNumber = tinNumber;
        this.pagIbigNumber = pagIbigNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public String getPhilHealthNumber() {
        return philHealthNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public String getPagIbigNumber() {
        return pagIbigNumber;
    }
}