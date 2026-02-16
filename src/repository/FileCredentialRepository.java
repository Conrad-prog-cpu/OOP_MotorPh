package repository;

import model.Role;
import model.UserAccount;

import java.io.*;
import java.util.*;

public class FileCredentialRepository implements CredentialRepository {

    private final String credentialsFilePath = "data/credentials.txt";

    private static class Rec {
        String id;
        String username;
        String password;
        String employeeNo;
        Role role;
    }

    @Override
    public List<UserAccount> findAll() {
        List<UserAccount> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Rec r = parse(line);
                if (r == null) continue;
                list.add(new UserAccount(r.id, r.username, r.role, r.employeeNo));
            }
        } catch (IOException e) {
            System.err.println("Error reading credentials: " + e.getMessage());
        }
        return list;
    }

    @Override
    public UserAccount findByUsername(String username) {
        Rec r = findRec(username);
        if (r == null) return null;
        return new UserAccount(r.id, r.username, r.role, r.employeeNo);
    }

    @Override
    public boolean add(String username, String password, Role role, String employeeNo) {
        if (role == null) role = Role.EMPLOYEE;

        File file = new File(credentialsFilePath);
        try {
            ensureFileEndsWithNewline(file);

            String newId = String.valueOf(getNextId());
            String line = String.join(",", newId, username, password, role.name(), employeeNo);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write(line);
                bw.newLine(); // ✅ guaranteed newline
            }
            return true;

        } catch (IOException e) {
            System.err.println("Error writing credentials: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String username) {
        // delete by returning null for matching record
        return rewrite(rec -> rec.username.equals(username) ? null : rec);
    }

    @Override
    public boolean updatePassword(String username, String newPassword) {
        return rewrite(rec -> {
            if (!rec.username.equals(username)) return rec;
            rec.password = newPassword;
            return rec;
        });
    }

    @Override
    public boolean updateRole(String username, Role newRole) {
        final Role roleToSet = (newRole == null) ? Role.EMPLOYEE : newRole;

        return rewrite(rec -> {
            if (!rec.username.equals(username)) return rec;
            rec.role = roleToSet;
            return rec;
        });
    }

    @Override
    public boolean updateEmployeeNo(String username, String newEmployeeNo) {
        final String empNo = (newEmployeeNo == null) ? "" : newEmployeeNo.trim();

        return rewrite(rec -> {
            if (!rec.username.equals(username)) return rec;
            rec.employeeNo = empNo;
            return rec;
        });
    }

    @Override
    public UserAccount validate(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Rec r = parse(line);
                if (r == null) continue;

                if (r.username.equals(username) && r.password.equals(password)) {
                    return new UserAccount(r.id, r.username, r.role, r.employeeNo);
                }
            }
        } catch (IOException e) {
            System.err.println("Error validating credentials: " + e.getMessage());
        }
        return null;
    }

    // ---------- helpers ----------

    private Rec findRec(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Rec r = parse(line);
                if (r != null && r.username.equals(username)) return r;
            }
        } catch (IOException ignore) {}
        return null;
    }

    private int getNextId() {
        int max = 0;
        File f = new File(credentialsFilePath);
        if (!f.exists()) return 1;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Rec r = parse(line);
                if (r == null) continue;
                try {
                    max = Math.max(max, Integer.parseInt(r.id));
                } catch (NumberFormatException ignore) {}
            }
        } catch (IOException ignore) {}
        return max + 1;
    }

    private Rec parse(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        String[] p = line.split(",", -1);
        if (p.length < 5) return null;

        Rec r = new Rec();
        r.id = p[0].trim();
        r.username = p[1].trim();
        r.password = p[2].trim();
        r.role = Role.from(p[3].trim());
        r.employeeNo = p[4].trim();
        return r;
    }

    private String format(Rec r) {
        return String.join(",", r.id, r.username, r.password, r.role.name(), r.employeeNo);
    }

    @FunctionalInterface
    private interface Mutator {
        // return updated record; return original to keep; return null to delete
        Rec apply(Rec rec);
    }

    private boolean rewrite(Mutator mutator) {
    List<String> out = new ArrayList<>();
    boolean changed = false;

    try (BufferedReader br = new BufferedReader(new FileReader(credentialsFilePath))) {
        String line;
        while ((line = br.readLine()) != null) {

            Rec rec = parse(line);
            if (rec == null) {
                out.add(line);
                continue;
            }

            // ✅ snapshot BEFORE mutation
            String before = format(rec);

            Rec updated = mutator.apply(rec);

            if (updated == null) { // delete
                changed = true;
                continue;
            }

            String after = format(updated);

            // ✅ detect change correctly
            if (!before.equals(after)) changed = true;

            out.add(after);
        }
        } catch (IOException e) {
            System.err.println("Error reading credentials: " + e.getMessage());
            return false;
        }

        if (!changed) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(credentialsFilePath))) {
            for (String l : out) {
                bw.write(l);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing credentials: " + e.getMessage());
            return false;
        }
    }



    private void ensureFileEndsWithNewline(File file) throws IOException {
        if (!file.exists() || file.length() == 0) return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(file.length() - 1);
            int last = raf.read();
            if (last != '\n') {
                try (FileWriter fw = new FileWriter(file, true)) {
                    fw.write(System.lineSeparator());
                }
            }
        }
    }
}
