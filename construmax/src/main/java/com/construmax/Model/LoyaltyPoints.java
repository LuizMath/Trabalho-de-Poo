package com.construmax.Model;

public class LoyaltyPoints {
    private int id;
    private int userId;
    private int points;
    private int totalContracts;
    private double totalSpent;
    
    // Constantes do sistema de fidelidade
    private static final int POINTS_PER_100_REAIS = 10; // 10 pontos a cada R$100
    private static final int MIN_CONTRACTS_FOR_VIP = 5; // 5 contratos para ser VIP
    private static final double MIN_SPENT_FOR_VIP = 5000.0; // R$5000 gastos para ser VIP
    private static final int POINTS_TO_REAL = 100; // 100 pontos = R$1 de desconto

    // Construtor vazio
    public LoyaltyPoints() {
        this.points = 0;
        this.totalContracts = 0;
        this.totalSpent = 0.0;
    }

    // Construtor com userId
    public LoyaltyPoints(int userId) {
        this.userId = userId;
        this.points = 0;
        this.totalContracts = 0;
        this.totalSpent = 0.0;
    }

    // Construtor completo
    public LoyaltyPoints(int id, int userId, int points, int totalContracts, double totalSpent) {
        this.id = id;
        this.userId = userId;
        this.points = points;
        this.totalContracts = totalContracts;
        this.totalSpent = totalSpent;
    }

    /**
     * Adiciona pontos baseado no valor do contrato
     * Regra: 10 pontos a cada R$100 gastos
     */
    public void addPoints(double contractValue) {
        int earnedPoints = (int) (contractValue / 100) * POINTS_PER_100_REAIS;
        this.points += earnedPoints;
        this.totalContracts++;
        this.totalSpent += contractValue;
    }

    /**
     * Usa pontos (desconta do saldo)
     * Retorna true se foi possível usar os pontos
     */
    public boolean usePoints(int pointsToUse) {
        if (pointsToUse <= 0 || pointsToUse > this.points) {
            return false;
        }
        
        // Só permite usar múltiplos de 100 pontos
        if (pointsToUse % 100 != 0) {
            return false;
        }
        
        this.points -= pointsToUse;
        return true;
    }

    /**
     * Calcula o valor do desconto disponível baseado nos pontos
     * 100 pontos = R$1 de desconto
     */
    public double calculateDiscount() {
        return (points / POINTS_TO_REAL) * 1.0;
    }

    /**
     * Verifica se o cliente deve ser promovido a VIP
     * Critérios: 5+ contratos OU R$5000+ gastos
     */
    public boolean shouldBeVIP() {
        return totalContracts >= MIN_CONTRACTS_FOR_VIP || totalSpent >= MIN_SPENT_FOR_VIP;
    }

    /**
     * Retorna o progresso para se tornar VIP (em porcentagem)
     */
    public int getVIPProgress() {
        double contractProgress = (totalContracts * 100.0) / MIN_CONTRACTS_FOR_VIP;
        double spentProgress = (totalSpent * 100.0) / MIN_SPENT_FOR_VIP;
        
        // Retorna o maior progresso
        return (int) Math.min(100, Math.max(contractProgress, spentProgress));
    }

    /**
     * Retorna quanto falta para ser VIP
     */
    public String getVIPRequirements() {
        if (shouldBeVIP()) {
            return "Você já é VIP!";
        }
        
        int contractsNeeded = Math.max(0, MIN_CONTRACTS_FOR_VIP - totalContracts);
        double spentNeeded = Math.max(0, MIN_SPENT_FOR_VIP - totalSpent);
        
        return String.format("Faltam %d contratos OU R$ %.2f gastos para ser VIP", 
                           contractsNeeded, spentNeeded);
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getTotalContracts() {
        return totalContracts;
    }

    public void setTotalContracts(int totalContracts) {
        this.totalContracts = totalContracts;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    // Getters para constantes (útil para exibição na UI)
    public static int getPointsPerReal() {
        return POINTS_PER_100_REAIS;
    }

    public static int getMinContractsForVIP() {
        return MIN_CONTRACTS_FOR_VIP;
    }

    public static double getMinSpentForVIP() {
        return MIN_SPENT_FOR_VIP;
    }

    public static int getPointsToReal() {
        return POINTS_TO_REAL;
    }

    @Override
    public String toString() {
        return "LoyaltyPoints{" +
                "id=" + id +
                ", userId=" + userId +
                ", points=" + points +
                ", totalContracts=" + totalContracts +
                ", totalSpent=" + totalSpent +
                ", isVIP=" + shouldBeVIP() +
                '}';
    }
}
