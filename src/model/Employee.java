package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract parent class for all Employee types.
 * This serves as the template (blueprint) for inheritance.
 */
public abstract class Employee {

    // ==========================
    // Identity Information
    // ==========================
    private String employeeID;
    private String lastName;
    private String firstName;
    private LocalDate birthday;
    private String address;
    private String phoneNumber;

    // Government IDs
    private String sssNumber;
    private String philHealthNumber;
    private String tinNumber;
    private String pagIbigNumber;

    // Employment Info
    private String status;
    private String position;
    private String immediateSupervisor;

    // Salary Information
    private BigDecimal basicSalary;
    private BigDecimal riceSubsidy;
    private BigDecimal phoneAllowance;
    private BigDecimal clothingAllowance;
    private BigDecimal semiMonthlyRate;
    private BigDecimal hourlyRate;

    // ==========================
    // Constructor
    // ==========================
    protected Employee(
            String employeeID,
            String lastName,
            String firstName,
            LocalDate birthday,
            String address,
            String phoneNumber,
            String sssNumber,
            String philHealthNumber,
            String tinNumber,
            String pagIbigNumber,
            String status,
            String position,
            String immediateSupervisor,
            BigDecimal basicSalary,
            BigDecimal riceSubsidy,
            BigDecimal phoneAllowance,
            BigDecimal clothingAllowance,
            BigDecimal semiMonthlyRate,
            BigDecimal hourlyRate) {

        this.employeeID = employeeID;
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
        this.semiMonthlyRate = semiMonthlyRate;
        this.hourlyRate = hourlyRate;
    }

    // ==========================
    // Encapsulated Getters
    // ==========================
    public String getEmployeeID() { return employeeID; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public LocalDate getBirthday() { return birthday; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilHealthNumber() { return philHealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagIbigNumber() { return pagIbigNumber; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }
    public String getImmediateSupervisor() { return immediateSupervisor; }

    public BigDecimal getBasicSalary() { return basicSalary; }
    public BigDecimal getRiceSubsidy() { return riceSubsidy; }
    public BigDecimal getPhoneAllowance() { return phoneAllowance; }
    public BigDecimal getClothingAllowance() { return clothingAllowance; }
    public BigDecimal getSemiMonthlyRate() { return semiMonthlyRate; }
    public BigDecimal getHourlyRate() { return hourlyRate; }

    // ==========================
    // Common Behavior
    // ==========================

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public BigDecimal getTotalAllowances() {
        return riceSubsidy
                .add(phoneAllowance)
                .add(clothingAllowance);
    }

    /**
     * Each employee type must define how their gross pay is computed.
     * @param hoursWorked
     * @return 
     */
    public abstract BigDecimal computeGrossPay(BigDecimal hoursWorked);

    /**
     * Each employee type may have different deduction rules.
     * @param hoursWorked
     * @return 
     */
    public abstract BigDecimal computeNetPay(BigDecimal hoursWorked);

    // ==========================
    // Equality (based on ID)
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeID, employee.employeeID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeID);
    }
}
