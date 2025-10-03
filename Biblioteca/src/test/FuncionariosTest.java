package test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.*;

import Dao.FuncionariosDao;
import Model.Funcionarios;



public class FuncionariosTest {

    private Funcionarios funcionarioTeste;

    @BeforeEach
    public void setup() {
        // Criar um funcionário para testar inserção e alteração
        funcionarioTeste = new Funcionarios();
        funcionarioTeste.setNm_funcionario("TesteJUnit");
        funcionarioTeste.setSenha("1234");
    }

    @Test
    public void testInserirEBuscarPorId() {
        Funcionarios inserido = FuncionariosDao.inserirFuncionario(funcionarioTeste);
        assertTrue(inserido.getCd_funcionario() > 0);

        Funcionarios buscado = FuncionariosDao.getByIdFuncionario(inserido.getCd_funcionario());
        assertNotNull(buscado);
        assertEquals("TesteJUnit", buscado.getNm_funcionario());
    }

    @Test
    public void testAlterarFuncionario() {
        Funcionarios inserido = FuncionariosDao.inserirFuncionario(funcionarioTeste);
        inserido.setNm_funcionario("TesteAlterado");
        inserido.setSenha("4321");

        FuncionariosDao.alterarFuncionarios(inserido);

        Funcionarios buscado = FuncionariosDao.getByIdFuncionario(inserido.getCd_funcionario());
        assertEquals("TesteAlterado", buscado.getNm_funcionario());
        assertEquals("4321", buscado.getSenha());
    }

    @Test
    public void testExcluirFuncionario() {
        Funcionarios inserido = FuncionariosDao.inserirFuncionario(funcionarioTeste);

        FuncionariosDao.excluirFuncionario(inserido.getCd_funcionario());

        Funcionarios buscado = FuncionariosDao.getByIdFuncionario(inserido.getCd_funcionario());
        assertNull(buscado);
    }

    @Test
    public void testGetAll() {
        List<Funcionarios> lista = FuncionariosDao.listarFuncionario();
        assertNotNull(lista);
        assertTrue(lista.size() >= 0);
    }

    @Test
    public void testAutenticarFuncionario() {
        Funcionarios inserido = FuncionariosDao.inserirFuncionario(funcionarioTeste);

        Funcionarios auth = FuncionariosDao.autenticarFuncionario("TesteJUnit", "1234");
        assertNotNull(auth);
        assertEquals(inserido.getCd_funcionario(), auth.getCd_funcionario());

        Funcionarios falha = FuncionariosDao.autenticarFuncionario("TesteJUnit", "senhaErrada");
        assertNull(falha);
    }
}
