package service;

public class EmployeeDetailsDto {

    private final String employeeId;
    private final String lastName;
    private final String firstName;
    private final String birthday;
    private final String address;
    private final String phoneNumber;
    private final String sssNumber;
    private final String philHealthNumber;
    private final String tinNumber;
    private final String pagIbigNumber;
    private final String status;
    private final String position;
    private final String immediateSupervisor;
    private final String basicSalary;
    private final String riceSubsidy;
    private final String phoneAllowance;
    private final String clothingAllowance;
    private final String grossSemiMonthlyRate;
    private final String hourlyRate;

    public EmployeeDetailsDto(
            String employeeId,
            String lastName,
            String firstName,
            String birthday,
            String address,
            String phoneNumber,
            String sssNumber,
            String philHealthNumber,
            String tinNumber,
            String pagIbigNumber,
            String status,
            String position,
            String immediateSupervisor,
            String basicSalary,
            String riceSubsidy,
            String phoneAllowance,
            String clothingAllowance,
            String grossSemiMonthlyRate,
            String hourlyRate
    ) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philHealthNumber = philHealthNumber;
        this.tinNumber = tinNumber;
        this.pagIbigNumber = pagIbigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;
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

    public String getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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

    public String getStatus() {
        return status;
    }

    public String getPosition() {
        return position;
    }

    public String getImmediateSupervisor() {
        return immediateSupervisor;
    }

    public String getBasicSalary() {
        return basicSalary;
    }

    public String getRiceSubsidy() {
        return riceSubsidy;
    }

    public String getPhoneAllowance() {
        return phoneAllowance;
    }

    public String getClothingAllowance() {
        return clothingAllowance;
    }

    public String getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public String getHourlyRate() {
        return hourlyRate;
    }

    public String getFieldValue(String fieldName) {
        return switch (fieldName) {
            case "Employee #", "Employee No." -> getEmployeeId();
            case "Last Name" -> getLastName();
            case "First Name" -> getFirstName();
            case "Birthday" -> getBirthday();
            case "Address" -> getAddress();
            case "Phone Number" -> getPhoneNumber();
            case "SSS #", "SSS No." -> getSssNumber();
            case "Philhealth #", "PhilHealth No." -> getPhilHealthNumber();
            case "TIN #", "TIN No." -> getTinNumber();
            case "Pag-ibig #", "PAG-IBIG No." -> getPagIbigNumber();
            case "Status" -> getStatus();
            case "Position" -> getPosition();
            case "Immediate Supervisor" -> getImmediateSupervisor();
            case "Basic Salary" -> getBasicSalary();
            case "Rice Subsidy" -> getRiceSubsidy();
            case "Phone Allowance" -> getPhoneAllowance();
            case "Clothing Allowance" -> getClothingAllowance();
            case "Gross Semi-monthly Rate" -> getGrossSemiMonthlyRate();
            case "Hourly Rate" -> getHourlyRate();
            default -> "";
        };
    }
}