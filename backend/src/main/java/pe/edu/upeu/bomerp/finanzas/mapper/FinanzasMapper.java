package pe.edu.upeu.bomerp.finanzas.mapper;

import pe.edu.upeu.bomerp.finanzas.dto.CategoriaFinancieraResponse;
import pe.edu.upeu.bomerp.finanzas.dto.CategoriaFinancieraResumen;
import pe.edu.upeu.bomerp.finanzas.dto.TransaccionFinancieraResponse;
import pe.edu.upeu.bomerp.finanzas.entity.CategoriaFinanciera;
import pe.edu.upeu.bomerp.finanzas.entity.TransaccionFinanciera;

public interface FinanzasMapper {

    CategoriaFinancieraResponse toCategoriaResponse(CategoriaFinanciera entity);

    CategoriaFinancieraResumen toCategoriaResumen(CategoriaFinanciera entity);

    TransaccionFinancieraResponse toTransaccionResponse(TransaccionFinanciera entity);
}
