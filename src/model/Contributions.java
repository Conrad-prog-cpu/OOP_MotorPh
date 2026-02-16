/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
/**
 *
 * @author ca
 */
public class Contributions {
    private final BigDecimal sss;
    private final BigDecimal philHealth;
    private final BigDecimal pagIbig;

    public Contributions(BigDecimal sss, BigDecimal philHealth, BigDecimal pagIbig) {
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
    }

    public BigDecimal getSss() { return sss; }
    public BigDecimal getPhilHealth() { return philHealth; }
    public BigDecimal getPagIbig() { return pagIbig; }

    public BigDecimal total() {
        return sss.add(philHealth).add(pagIbig);
    }
}