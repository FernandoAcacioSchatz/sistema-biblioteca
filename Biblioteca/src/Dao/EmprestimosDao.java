package Dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import Model.Emprestimos;
import View.ConexaoBD;

public class EmprestimosDao {

    /*
     * REgista um emprestimo de livro
     * no banco de dados
     */

    public static void registrarEmprestimo(Emprestimos e) {
        String sql = "INSERT INTO emprestimos(cd_livro, cd_usuario, cd_funcionario, dt_emprestimo, dt_devolucao_prevista) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {

            stm.setInt(1, e.getCd_livro());
            stm.setInt(2, e.getCd_usuario());
            stm.setInt(3, e.getCd_funcionario());
            stm.setDate(4, java.sql.Date.valueOf(e.getDt_emprestimo()));
            stm.setDate(5, java.sql.Date.valueOf(e.getDt_devolucao_prevista()));

            int linhasAfetadas = stm.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new RuntimeException("Falha ao registrar o empréstimo: nenhuma linha afetada.");
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao registrar empréstimo: " + ex.getMessage(), ex);
        }
    }

    /*
     * registra a devolução do livro
     * no banco de dados
     */
    public static void registrarDevolucao(int cdEmprestimo, LocalDate dtDevolucaoReal) {
        String sql = "UPDATE emprestimos SET dt_devolucao_real = ? WHERE cd_emprestimo = ?";

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {

            stm.setDate(1, java.sql.Date.valueOf(dtDevolucaoReal));
            stm.setInt(2, cdEmprestimo);
            int linhasAfetadas = stm.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhum empréstimo encontrado para devolução.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar devolução no banco de dados.", e);
        }
    }

    /*
     * Verifica, no banco de dados, se um livro esta emprestado ou não
     * Se tiver, retorna os dados do emprestimo
     */
    public static Emprestimos buscarEmprestimoAtivoPorLivro(int cdLivro) {
        String sql = """
                SELECT e.cd_emprestimo, e.cd_usuario, u.nm_usuario, e.dt_emprestimo, e.dt_devolucao_prevista
                FROM emprestimos e
                JOIN usuarios u ON u.cd_usuario = e.cd_usuario
                WHERE e.cd_livro = ? AND e.dt_devolucao_real IS NULL
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {

            stm.setInt(1, cdLivro);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                Emprestimos e = new Emprestimos();
                e.setCd_emprestimo(rs.getInt("cd_emprestimo"));
                e.setCd_livro(cdLivro);
                e.setCd_usuario(rs.getInt("cd_usuario"));
                e.setNomeUsuario(rs.getString("nm_usuario"));
                e.setDt_emprestimo(rs.getDate("dt_emprestimo").toLocalDate());
                e.setDt_devolucao_prevista(rs.getDate("dt_devolucao_prevista").toLocalDate());
                return e;
            } else {
                throw new RuntimeException("Nenhum emprestimo ativo para esse livro");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar empréstimo ativo: " + e.getMessage());
        }

    }

    /*
     * Lista empréstimos que
     * estão ativos
     */
    public static List<Emprestimos> listarEmprestimosNaoDevolvidos() {
        List<Emprestimos> lista = new ArrayList<>();

        String sql = """
                SELECT e.cd_emprestimo,
                       e.cd_livro,
                       l.titulo_livro AS nome_livro,
                       e.cd_usuario,
                       u.nm_usuario AS nome_usuario,
                       e.cd_funcionario,
                       f.nm_funcionario AS nome_funcionario,
                       e.dt_emprestimo,
                       e.dt_devolucao_prevista
                FROM emprestimos e
                JOIN livros l ON l.cd_livro = e.cd_livro
                JOIN usuarios u ON u.cd_usuario = e.cd_usuario
                JOIN funcionarios f ON f.cd_funcionario = e.cd_funcionario
                WHERE e.dt_devolucao_real IS NULL
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                Emprestimos e = new Emprestimos();
                e.setCd_emprestimo(rs.getInt("cd_emprestimo"));
                e.setCd_livro(rs.getInt("cd_livro"));
                e.setNomeLivro(rs.getString("nome_livro"));
                e.setCd_usuario(rs.getInt("cd_usuario"));
                e.setNomeUsuario(rs.getString("nome_usuario"));
                e.setCd_funcionario(rs.getInt("cd_funcionario"));
                e.setNomeFuncionario(rs.getString("nome_funcionario"));
                e.setDt_emprestimo(rs.getDate("dt_emprestimo").toLocalDate());
                e.setDt_devolucao_prevista(rs.getDate("dt_devolucao_prevista").toLocalDate());

                lista.add(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar emoréstimos não devolvidos" + e.getMessage());

        }

        return lista;
    }

    /*
     * Lista empréstimos não
     * devolvidos e estão atrasados
     */

    public static List<Emprestimos> listarEmprestimosPendentes() {
        List<Emprestimos> lista = new ArrayList<>();

        String sql = """
                SELECT e.cd_emprestimo,
                       e.cd_livro,
                       l.titulo_livro AS nome_livro,
                       e.cd_usuario,
                       u.nm_usuario AS nome_usuario,
                       e.dt_emprestimo,
                       e.dt_devolucao_prevista
                FROM emprestimos e
                JOIN livros l ON l.cd_livro = e.cd_livro
                JOIN usuarios u ON u.cd_usuario = e.cd_usuario
                WHERE e.dt_devolucao_real IS NULL
                  AND e.dt_devolucao_prevista < CURRENT_DATE
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                Emprestimos e = new Emprestimos();
                e.setCd_emprestimo(rs.getInt("cd_emprestimo"));
                e.setCd_livro(rs.getInt("cd_livro"));
                e.setNomeLivro(rs.getString("nome_livro"));
                e.setCd_usuario(rs.getInt("cd_usuario"));
                e.setNomeUsuario(rs.getString("nome_usuario"));
                e.setDt_emprestimo(rs.getDate("dt_emprestimo").toLocalDate());
                e.setDt_devolucao_prevista(rs.getDate("dt_devolucao_prevista").toLocalDate());

                lista.add(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar empréstimos pendentes: " + e.getMessage(), e);
        }

        return lista;
    }

    /*
     * Lista histórico de empréstimos
     * de um determinado usuário
     */
    public static List<Emprestimos> listarHistoricoPorUsuario(int cdUsuario) {
        List<Emprestimos> lista = new ArrayList<>();
        String sql = """
                SELECT e.cd_emprestimo, e.cd_livro, l.titulo_livro, e.dt_emprestimo, e.dt_devolucao_real
                FROM emprestimos e
                JOIN livros l ON l.cd_livro = e.cd_livro
                WHERE e.cd_usuario = ?
                ORDER BY e.dt_emprestimo DESC
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {

            stm.setInt(1, cdUsuario);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                Emprestimos e = new Emprestimos();
                e.setCd_emprestimo(rs.getInt("cd_emprestimo"));
                e.setCd_livro(rs.getInt("cd_livro"));
                e.setNomeLivro(rs.getString("titulo_livro"));
                e.setDt_emprestimo(rs.getDate("dt_emprestimo").toLocalDate());

                if (rs.getDate("dt_devolucao_real") != null) {
                    e.setDt_devolucao_real(rs.getDate("dt_devolucao_real").toLocalDate());
                }

                lista.add(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar histórico de empréstimos do usuário (ID " + cdUsuario + "): " + e.getMessage(), e);
        }

        return lista;
    }

    /*
     * Consulta se o usuário tem
     * emprestimos ativos
     */
    public static boolean usuarioTemEmprestimosAtivos(int cdUsuario) {
        String sql = """
                    SELECT 1
                    FROM emprestimos
                    WHERE cd_usuario = ?
                      AND dt_devolucao_real IS NULL
                    LIMIT 1
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {

            stm.setInt(1, cdUsuario);
            ResultSet rs = stm.executeQuery();

            return rs.next(); // se existir ao menos 1 linha, retorna true

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao verificar empréstimos ativos do usuário (ID " + cdUsuario + "): " + e.getMessage(), e);
        }
    }

    /*
     * Consulta se o usuário tem
     * emprestimos pendentes
     */
    public static boolean usuarioTemEmprestimosPendentes(int cdUsuario) {
        String sql = """
                    SELECT 1 FROM emprestimos
                    WHERE cd_usuario = ?
                      AND dt_devolucao_real IS NULL
                      AND dt_devolucao_prevista < CURRENT_DATE
                    LIMIT 1
                """;

        try (Connection con = ConexaoBD.getConexao();
                PreparedStatement stm = con.prepareStatement(sql)) {
            stm.setInt(1, cdUsuario);
            ResultSet rs = stm.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar pendêmcias" + e.getMessage());
            
        }
    }

    public static int contarEmprestimosAtivos(int cdUsuario) {
    String sql = """
                SELECT COUNT(*) AS total
                FROM emprestimos
                WHERE cd_usuario = ?
                  AND dt_devolucao_real IS NULL
            """;

    try (Connection con = ConexaoBD.getConexao();
         PreparedStatement stm = con.prepareStatement(sql)) {
        stm.setInt(1, cdUsuario);
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            return rs.getInt("total");
        } else {
            // Caso improvável, mas para garantir retorno, pode lançar exceção ou retornar 0
            return 0;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao contar empréstimos ativos do usuário (ID " + cdUsuario + "): " + e.getMessage(), e);
    }
}


    /*
     * Consulta se o Livro
     * está emprestado
     */
    public static boolean livroEstaEmprestado(int cdLivro) {
    String sql = "SELECT COUNT(*) FROM emprestimos WHERE cd_livro = ? AND dt_devolucao_real IS NULL";
    try (Connection con = ConexaoBD.getConexao();
         PreparedStatement stm = con.prepareStatement(sql)) {
        stm.setInt(1, cdLivro);
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0; // true se houver empréstimo ativo para o livro
        } else {
            // Caso improvável, pode retornar false ou lançar exceção
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Erro ao verificar se livro (ID " + cdLivro + ") está emprestado: " + e.getMessage(), e);
    }
}


}
