import gui.LoginPanel;

import model.ContributionCalculator;
import model.DefaultContributionCalculator;
import model.DefaultEarningsCalculator;
import model.DefaultTaxCalculator;
import model.DefaultTaxableBenefitsPolicy;
import model.DefaultWorkHoursCalculator;
import model.EarningsCalculator;
import model.MotorPHOvertimePolicy;
import model.OvertimePolicy;
import model.TaxCalculator;
import model.TaxableBenefitsPolicy;
import model.WorkHoursCalculator;

import repository.AttendanceRepository;
import repository.CredentialRepository;
import repository.EmployeeRepository;
import repository.FileAttendanceRepository;
import repository.FileCredentialRepository;
import repository.FileEmployeeRepository;
import repository.FileLeaveRepository;
import repository.LeaveRepository;

import service.AuthService;
import service.DefaultAuthService;
import service.DefaultEmployeeService;
import service.DefaultLeaveService;
import service.DefaultPayrollService;
import service.DefaultUserAccountService;
import service.EmployeeService;
import service.LeaveService;
import service.PayrollService;
import service.UserAccountService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

    public static void main(String[] args) {
        setNimbusLookAndFeel();

        // =========================
        // REPOSITORIES
        // =========================
        CredentialRepository credentialRepository = new FileCredentialRepository();
        EmployeeRepository employeeRepository = new FileEmployeeRepository();
        AttendanceRepository attendanceRepository = new FileAttendanceRepository();
        LeaveRepository leaveRepository = new FileLeaveRepository();

        // =========================
        // OPTIONAL EAGER LOAD
        // =========================
        credentialRepository.load();
        employeeRepository.load();
        attendanceRepository.load();
//        leaveRepository.load();

        // =========================
        // DOMAIN / BUSINESS RULES
        // =========================
        OvertimePolicy overtimePolicy = new MotorPHOvertimePolicy();
        WorkHoursCalculator workHoursCalculator = new DefaultWorkHoursCalculator(overtimePolicy);
        EarningsCalculator earningsCalculator = new DefaultEarningsCalculator();
        ContributionCalculator contributionCalculator = new DefaultContributionCalculator();
        TaxableBenefitsPolicy taxableBenefitsPolicy = new DefaultTaxableBenefitsPolicy();
        TaxCalculator taxCalculator = new DefaultTaxCalculator();

        // =========================
        // SERVICES
        // =========================
        AuthService authService = new DefaultAuthService(
                credentialRepository,
                employeeRepository
        );

        EmployeeService employeeService = new DefaultEmployeeService(
                employeeRepository,
                attendanceRepository
        );

        UserAccountService userAccountService = new DefaultUserAccountService(
                credentialRepository,
                employeeRepository
        );

        LeaveService leaveService = new DefaultLeaveService(
                leaveRepository
        );

        PayrollService payrollService = new DefaultPayrollService(
                employeeRepository,
                attendanceRepository,
                workHoursCalculator,
                earningsCalculator,
                contributionCalculator,
                taxCalculator,
                overtimePolicy,
                taxableBenefitsPolicy
        );

        // =========================
        // GUI
        // =========================
        SwingUtilities.invokeLater(() -> {
            LoginPanel loginPanel = new LoginPanel(
                    authService,
                    employeeService,
                    userAccountService,
                    leaveService,
                    payrollService
            );
            loginPanel.setVisible(true);
        });
    }

    private static void setNimbusLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | UnsupportedLookAndFeelException ignored) {
        }
    }
}