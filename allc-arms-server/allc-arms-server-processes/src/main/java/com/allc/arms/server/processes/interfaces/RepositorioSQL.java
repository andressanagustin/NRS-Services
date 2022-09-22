package com.allc.arms.server.processes.interfaces;

import com.allc.arms.server.persistence.item.ItemTmp;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.hibernate.HibernateException;

public class RepositorioSQL {

    static Logger log = Logger.getLogger(RepositorioSQL.class);
    private Session sessionArtsEc = null;

    public RepositorioSQL(Session sessionParam) {
        this.sessionArtsEc = sessionParam;
    }

    /**
     * ******************************************************************************************************************************************
     */
    /**
     * ****************************************************	CONSULTA
     * *********************************************************
     */
    /**
     * ******************************************************************************************************************************************
     */
    public boolean consulta_as_itm_load_file(String fileName) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_file, descripcion, estado, ejecutado, var_procesado, directorio_file, valida_php, fecha FROM AS_ITM_LOAD_FILE WHERE DESCRIPCION =  '" + fileName + "'");
            List<Object[]> rows = query.list();
            return rows != null && !rows.isEmpty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Object[] consulta_fila_as_itm_load_file(String fileName) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_file, descripcion, estado, ejecutado, var_procesado, directorio_file, valida_php, fecha FROM AS_ITM_LOAD_FILE WHERE DESCRIPCION =  '" + fileName + "' and date(fecha) = date(now())");
            return (Object[]) query.uniqueResult();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    /// Consultamos

    public List<Object[]> consulta_ejecutando_all() {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_FILE, DESCRIPCION, ESTADO, EJECUTADO, VAR_PROCESADO, DIRECTORIO_FILE, VALIDA_PHP, FECHA FROM AS_ITM_LOAD_FILE WHERE ESTADO = 'false' AND EJECUTADO = 'false'");
            List<Object[]> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    public Object[] consulta_as_itm_x_codigo(String cdItm) {
        try {

            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_ITM,DE_ITM,DE_DESCRIPCION,EDAD,ALTO,ANCHO,LARGO,PESO,GENERO,COLOR,DISENIADOR FROM AS_ITM WHERE ltrim(cd_itm,'0') = ltrim('" + cdItm + "','0')");
            List<Object[]> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    public Object[] consulta_as_itm_x_codigo_jgt(String cdItm) {
        try {
            //                                                        0      1      2             3    4    5     6     7     8     9      10         11
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_ITM,DE_ITM,DE_DESCRIPCION,EDAD,ALTO,ANCHO,LARGO,PESO,GENERO,COLOR,DISENIADOR,PESO_SERVIENTREGA FROM AS_ITM WHERE ltrim(cd_itm,'0') = ltrim('" + cdItm + "','0')");
            List<Object[]> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Object[] consulta_as_itm_imagen_nombre1(long idItm, String nombre) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("select ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL, IMAGEN_SM, ORDEN_PRESENTACION from AS_ITM_IMAGEN where id_itm = " + idItm + " AND imagen = '" + nombre + "'");
            List<Object[]> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Object[] consulta_as_itm_imagen_registro(long idItm) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("select ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL, IMAGEN_SM, ORDEN_PRESENTACION from AS_ITM_IMAGEN where id_itm = " + idItm + "order by id_itm_imagen desc");
            List<Object[]> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public boolean borra_as_itm_imagen_registro(long idItm) {

        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("delete from AS_ITM_IMAGEN where id_itm =:valor1");
            query.setParameter("valor1", idItm);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;

    }

    public BigInteger consulta_co_spr_detalle(String de_spr) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("select id_spr from co_spr where UPPER(de_spr) like '%" + de_spr + "%'");
            List<BigInteger> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_co_mrhrc_gp_x_cd_mrhrc_gp_id_dpt_ps(int cd_mrhrc_gp, int id_dpt_ps) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_MRHRC_GP FROM CO_MRHRC_GP WHERE CD_MRHRC_GP = " + cd_mrhrc_gp + " AND ID_DPT_PS = " + id_dpt_ps);
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List<Integer> consulta_id_bsn_un_x_codigo(long cd_str_rt) {
        try {
            //id_bsn_un, de_str_rt, cd_str_rt, iva_tax, inc_prc, imp_1, imp_2, imp_3, imp_4, imp_5, imp_6, imp_7, imp_8, ce_cobe, no_afil_fl, id_ctab, dist_dir, fl_stock_load 
            Query query = this.sessionArtsEc.createSQLQuery("select id_bsn_un FROM PA_STR_RTL where cd_str_rt = " + cd_str_rt);
            List<Integer> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List<Object[]> consulta_id_bsn_un_stock_load() {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_bsn_un,de_str_rt,cd_str_rt,fl_stock_load FROM pa_str_rtl");
            List<Object[]> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public boolean consulta_id_bsn_un_stock_load(String tienda) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT fl_stock_load FROM pa_str_rtl where cd_str_rt = '" + tienda + "'");
            List<Boolean> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public boolean consulta_as_itm_load_file_stock(String descripcion) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_file FROM AS_ITM_LOAD_FILE_STOCK where descripcion = '" + descripcion + "'");
            List<Object[]> rows = query.list();
            return rows != null && !rows.isEmpty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List<Object[]> consulta_as_itm_load_file_stock_procesar() {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_FILE, DESCRIPCION, PROCESADO, DIRECTORIO_FILE, ERROR, FECHA FROM AS_ITM_LOAD_FILE_STOCK WHERE PROCESADO = 'false' AND ERROR='false' ORDER BY fecha ");
            List<Object[]> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Object[] consulta_as_itm_str_codigos(long id_itm, long id_bsn_un) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("select id_itm, id_bsn_un, marca FROM AS_ITM_STR where id_itm = " + id_itm + " AND id_bsn_un = " + id_bsn_un);
            List<Object[]> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public String consulta_id_ps(long id_itm, long id_bsn_un) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("select ID_ITM_PS FROM ID_PS where ID_ITM = " + id_itm + " AND ID_BSN_UN = " + id_bsn_un);
            List<String> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_ecommerce_catalogo(String subcategoria, Integer idListCatalogo) {
        try {
            //select id_catalogo from arts_ec.ecommerce_catalogo where nombre_en = 'Basureros & papeleras'
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_CATALOGO FROM ecommerce_catalogo WHERE LOWER(nombre_en) = LOWER('" + subcategoria + "') and estado = 1 and id_listado_catalogo = " + idListCatalogo);
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_ecommerce_catalogo(String subcategoria, Integer idListCatalogo, Integer idCatPadre) {
        try {
            //select id_catalogo from arts_ec.ecommerce_catalogo where nombre_en = 'Basureros & papeleras'
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_CATALOGO FROM ecommerce_catalogo WHERE LOWER(nombre_en) = LOWER('" + subcategoria + "') and estado = 1 and id_listado_catalogo = " + idListCatalogo + " AND id_catalogo_padre = " + idCatPadre);
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_id_listado_catalogo(String nombre) {
        try {
            //select id_listado_catalogo from arts_ec.ecommerce_listado_catalogo where UPPER(nombre_en) like '%MOBLART%' and estado = 1
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_listado_catalogo FROM ecommerce_listado_catalogo WHERE UPPER(nombre_en) like '%" + nombre + "%' and estado = 1");
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_ecommerce_catalogo_nodo_padre(Integer idListCatalogo) {
        try {
            //select ID_CATALOGO from arts_ec.ecommerce_catalogo where id_catalogo_padre = 0 and id_listado_catalogo = 9
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_CATALOGO FROM ecommerce_catalogo WHERE id_catalogo_padre = 0 and estado = 1 and id_listado_catalogo = " + idListCatalogo);
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Integer consulta_ecommerce_catalogo_ultimo_ID(Integer idListCatalogo) {
        try {
            //select MAX(id_catalogo) from arts_ec.ecommerce_catalogo where id_listado_catalogo = 9 
            Query query = this.sessionArtsEc.createSQLQuery("SELECT MAX(id_catalogo) FROM ecommerce_catalogo WHERE id_listado_catalogo = " + idListCatalogo);
            List<Integer> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Short consulta_ecommerce_catalogo_items_estado(Integer idListCatalogo, Integer idCatalogo, Long idItm) {
        try {
            //select estado from arts_ec.ecommerce_catalogo_items where id_listado_catalogo = 9 AND id_catalogo = 20 and id_itm = 168580 
            Query query = this.sessionArtsEc.createSQLQuery("SELECT estado FROM ecommerce_catalogo_items WHERE id_listado_catalogo = " + idListCatalogo + "  AND id_catalogo = " + idCatalogo + " and id_itm = " + idItm);
            List<Short> rows = query.list();
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Object consulta_id_dpt_ps(Integer id_dpt_ps) {
        Query query = this.sessionArtsEc.createSQLQuery("SELECT id_dpt_ps FROM id_dpt_ps WHERE id_dpt_ps = " + id_dpt_ps);
        return query.uniqueResult();
    }

    /**
     * ******************************************************************************************************************************************
     */
    /**
     * ****************************************************	INSERTA
     * *********************************************************
     */
    /**
     * Inserta un registro para conrtrol de archivos procesados
     *
     * @param fileName
     * @param urlFolder
     * @param proccess
     * @return
     */
    public boolean inserta_as_itm_load_file(String fileName, String urlFolder, boolean proccess) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_LOAD_FILE (DESCRIPCION, ESTADO, EJECUTADO, VAR_PROCESADO, DIRECTORIO_FILE, VALIDA_PHP, FECHA, RVW_ECOM) VALUES (:valor1,false,false,0,:valor2,false,NOW(), :valor3)");
            //Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO ARC_MOV (ID_ARCPRC, ID_ESTPRC, HOR_MOV) VALUES (:valor1, :valor2, :valor3)");
            query.setParameter("valor1", fileName);
            query.setParameter("valor2", urlFolder);
            query.setBoolean("valor3", proccess);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_as_itm_imagen(long id_itm_imagen, long id_itm, String imagen) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_IMAGEN(ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL) VALUES (:valor1, :valor2, :valor3, 1, NOW(), 1)");
            query.setParameter("valor1", id_itm_imagen);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", imagen);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*public boolean inserta_as_itm_imagen(long id_itm_imagen,long id_itm,String imagen,String imagenSm) {
		Transaction tx = null;
		try {
			tx = this.sessionArtsEc.beginTransaction();
			// agregar orden_presentacion
			Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_IMAGEN(ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL, IMAGEN_SM) VALUES (:valor1, :valor2, :valor3, 1, NOW(), 1, :valor4)");
			query.setParameter("valor1", id_itm_imagen);
			query.setParameter("valor2", id_itm);
			query.setParameter("valor3", imagen);
			query.setParameter("valor4", imagenSm);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}*/
    public boolean inserta_as_itm_imagen2(long id_itm_imagen, long id_itm, String imagen, String imagenSm, int orden, int principal) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            // agregar orden_presentacion
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_IMAGEN(ID_ITM_IMAGEN, ID_ITM, IMAGEN, ESTADO, FECHA_CREACION, PRINCIPAL, IMAGEN_SM, ORDEN_PRESENTACION) VALUES (:valor1, :valor2, :valor3, 1, NOW(), :valor6, :valor4, :valor5)");
            query.setParameter("valor1", id_itm_imagen);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", imagen);
            query.setParameter("valor4", imagenSm);
            query.setParameter("valor5", orden);
            query.setParameter("valor6", principal);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_co_spr(String de_spr) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO CO_SPR (de_spr,estado,fecha_creacion) values (:valor1,1,NOW())");
            query.setParameter("valor1", de_spr);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_as_itm(String cd_itm, String nm_itm, boolean fl_itm_dsc, boolean fl_rp_rq, int fl_wm_rq, boolean fl_qy_rq, boolean fl_qy_alw, String de_itm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM(CD_ITM, NM_ITM, FL_ITM_DSC, FL_RP_RQ, FL_WM_RQ, FL_QY_RQ, FL_QY_ALW, ID_TY_ITM, DE_ITM, FLAG_EC) "
                    + "VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, 0, :valor8, 1)");
            query.setParameter("valor1", cd_itm);
            query.setParameter("valor2", nm_itm);
            query.setParameter("valor3", fl_itm_dsc);
            query.setParameter("valor4", fl_rp_rq);
            query.setParameter("valor5", fl_wm_rq);
            query.setParameter("valor6", fl_qy_rq);
            query.setParameter("valor7", fl_qy_alw);
            query.setParameter("valor8", de_itm);
            query.executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
    }

    public boolean inserta_as_itm_api(String cd_itm, String nm_itm, boolean fl_itm_dsc,
            boolean fl_rp_rq, int fl_wm_rq, boolean fl_qy_rq, boolean fl_qy_alw,
            String de_itm, String de_desc, Integer id_mrhrc_gp, Double alto, Double ancho, Double largo,
            String color, double peso, String diseniador, boolean req_armado) {
        Transaction tx = null;
        try {
            Query query1 = this.sessionArtsEc.createSQLQuery("select max(id_itm) FROM AS_ITM");
            Integer maxId = ((BigInteger) query1.uniqueResult()).intValue();
            tx = this.sessionArtsEc.beginTransaction();
                                                                        //                1        2       3           4          5        6        7          v0         8        v1       9            10             11     12    13      14    15     16
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM(id_itm, CD_ITM, NM_ITM, FL_ITM_DSC, FL_RP_RQ, FL_WM_RQ, FL_QY_RQ, FL_QY_ALW, ID_TY_ITM, DE_ITM, FLAG_EC, ID_MRHRC_GP, DE_DESCRIPCION, ALTO, ANCHO, LARGO, COLOR, PESO , DISENIADOR, FL_REQ_ARM) "
                    + "VALUES (:maxId, :valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, 0, :valor8, 1, :valor9, :valor10, :valor11, :valor12, :valor13, :valor14, :valor15, :valor16, :valor17)");
            query.setParameter("maxId", maxId + 1);
            query.setParameter("valor1", cd_itm);
            query.setParameter("valor2", nm_itm);
            query.setParameter("valor3", fl_itm_dsc);
            query.setParameter("valor4", fl_rp_rq);
            query.setParameter("valor5", fl_wm_rq);
            query.setParameter("valor6", fl_qy_rq);
            query.setParameter("valor7", fl_qy_alw);
            query.setParameter("valor8", de_itm);
            query.setParameter("valor9", id_mrhrc_gp);
            query.setParameter("valor10", de_desc);
            query.setParameter("valor11", alto);
            query.setParameter("valor12", ancho);
            query.setParameter("valor13", largo);
            query.setParameter("valor14", color);
            query.setParameter("valor15", peso);
            query.setParameter("valor16", diseniador);
            query.setParameter("valor17", req_armado);
            query.executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
    }

    public boolean inserta_as_itm_mrhrc(String cd_itm, String nm_itm, long id_mrhrc_gp, boolean fl_itm_dsc, boolean fl_rp_rq, int fl_wm_rq, boolean fl_qy_rq, boolean fl_qy_alw, String de_itm) {
        Transaction tx = null;
        String consulta = null;
        try {

            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM(CD_ITM, NM_ITM, ID_MRHRC_GP,FL_ITM_DSC, FL_RP_RQ, FL_WM_RQ, FL_QY_RQ, FL_QY_ALW, ID_TY_ITM, DE_ITM, FLAG_EC, DT_CREATE_CODIPDV) "
                    + "VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8, 0, :valor9, 1, NOW())");

            query.setParameter("valor1", cd_itm);
            query.setParameter("valor2", nm_itm);
            query.setParameter("valor3", id_mrhrc_gp);
            query.setParameter("valor4", fl_itm_dsc);
            query.setParameter("valor5", fl_rp_rq);
            query.setParameter("valor6", fl_wm_rq);
            query.setParameter("valor7", fl_qy_rq);
            query.setParameter("valor8", fl_qy_alw);
            query.setParameter("valor9", de_itm);
            consulta = "Query:inserta_as_itm_mrhrc; - " + query.getQueryString() + ";;valor1:" + cd_itm + ";valor2:" + nm_itm + ";valor3:" + id_mrhrc_gp + ";valor4:" + fl_itm_dsc + ";valor5:" + fl_rp_rq + ";valor6:" + fl_wm_rq + ";valor7:" + fl_qy_rq + ";valor8:" + fl_qy_alw + ";valor9:" + de_itm;
            //log.info("Query:inserta_as_itm_mrhrc; - " + query.getQueryString());
            //log.info("valor1:"+cd_itm+";valor2:"+nm_itm+";valor3:"+id_mrhrc_gp+";valor4:"+fl_itm_dsc+";valor5:"+fl_rp_rq+";valor6:"+fl_wm_rq+";valor7:"+fl_qy_rq+";valor8:"+fl_qy_alw+";valor9:"+de_itm);
            query.executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (consulta != null) {
                log.info(consulta);
            }
            tx.rollback();
            return false;
        }
    }

    public boolean inserta_as_itm_str(long id_itm, long id_bsn_un, float sls_prc, boolean fl_azn_fr_sls, int TX_A, int FLG_MAY, int QTY_MAY, float PRC_MAY, String COLOR, String DISENO, String MARCA, String PRESENTACION, float STOCK) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_STR(ID_ITM, ID_BSN_UN, SLS_PRC, FL_AZN_FR_SLS, TX_A, FLG_MAY, QTY_MAY, PRC_MAY, COLOR, DISENO, MARCA, PRESENTACION, STOCK, FEC_PRC_DAT) "
                    + "VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8, :valor9, :valor10, :valor11, :valor12, :valor13, :valor14)");
            query.setParameter("valor1", id_itm);
            query.setParameter("valor2", id_bsn_un);
            query.setParameter("valor3", sls_prc);
            query.setParameter("valor4", fl_azn_fr_sls);
            query.setParameter("valor5", TX_A);
            query.setParameter("valor6", FLG_MAY);
            query.setParameter("valor7", QTY_MAY);
            query.setParameter("valor8", PRC_MAY);
            query.setParameter("valor9", COLOR);
            query.setParameter("valor10", DISENO);
            query.setParameter("valor11", MARCA);
            query.setParameter("valor12", PRESENTACION);
            query.setParameter("valor13", STOCK);
            query.setTimestamp("valor14", new Date());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /**
     * @param id_itm
     * @param id_bsn_un
     * @param TX_A
     * @param fl_azn_fr_sls
     * @param sls_prc
     * @param FLG_MAY
     * @param QTY_MAY
     * @param MARCA
     * @param COLOR
     * @param DISENO
     * @param PRC_MAY
     * @param PRESENTACION
     * @param STOCK
     * @return
     */
    public boolean inserta_as_itm_str_api(long id_itm, long id_bsn_un, float sls_prc, boolean fl_azn_fr_sls, int TX_A, int FLG_MAY, int QTY_MAY, float PRC_MAY, String COLOR, String DISENO, String MARCA, String PRESENTACION, float STOCK) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_STR(ID_ITM, ID_BSN_UN, SLS_PRC, FL_AZN_FR_SLS, TX_A, FLG_MAY, QTY_MAY, PRC_MAY, COLOR, DISENO, MARCA, PRESENTACION, STOCK, FEC_PRC_DAT, FEC_PRC_STOCK) "
                    + "VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8, :valor9, :valor10, :valor11, :valor12, :valor13, :valor14, :valor15)");
            query.setParameter("valor1", id_itm);
            query.setParameter("valor2", id_bsn_un);
            query.setParameter("valor3", sls_prc);
            query.setParameter("valor4", fl_azn_fr_sls);
            query.setParameter("valor5", TX_A);
            query.setParameter("valor6", FLG_MAY);
            query.setParameter("valor7", QTY_MAY);
            query.setParameter("valor8", PRC_MAY);
            query.setParameter("valor9", COLOR);
            query.setParameter("valor10", DISENO);
            query.setParameter("valor11", MARCA);
            query.setParameter("valor12", PRESENTACION);
            query.setParameter("valor13", STOCK);
            query.setTimestamp("valor14", new Date());
            query.setTimestamp("valor15", new Date());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_id_ps(String id_itm_ps, long id_itm, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO ID_PS(ID_ITM_PS, ID_ITM, ID_BSN_UN, PRTY)	VALUES (:valor1, :valor2, :valor3, 'P')");
            query.setParameter("valor1", id_itm_ps);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", id_bsn_un);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_as_itm_load_file_stock(String descripcion, String directorio_file) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO AS_ITM_LOAD_FILE_STOCK (descripcion,procesado,directorio_file,error,fecha) values (:valor1,false,:valor2,false,NOW())");
            query.setParameter("valor1", descripcion);
            query.setParameter("valor2", directorio_file);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_ecommerce_catalogo_items(Integer idListCatalogo, Integer idCatalogo, Long idItm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO ecommerce_catalogo_items(id_listado_catalogo, id_catalogo, id_itm, estado, favorito,fecha) VALUES (:valor1, :valor2, :valor3, 1, 0,CURRENT_DATE)");
            query.setParameter("valor1", idListCatalogo);
            query.setParameter("valor2", idCatalogo);
            query.setParameter("valor3", idItm);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_ecommerce_catalogo(Integer idCatalogo, Integer idListCatalogo, Integer idCatPadre, String nombre) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO ecommerce_catalogo(id_catalogo, id_listado_catalogo, id_catalogo_padre, nombre_en, nombre_es, img_home, img_lateral, flag_menu_lateral, flag_filtro_busq,flag_home,estado, fecha_creacion,orden,img_banner_horizontal,icono,flag_header) VALUES (:valor1, :valor2, :valor3, :valor4, :valor4, NULL, NULL, 1, 1, 1, 1, NOW(), NULL, NULL, NULL, 0);");
            query.setParameter("valor1", idCatalogo);
            query.setParameter("valor2", idListCatalogo);
            query.setParameter("valor3", idCatPadre);
            query.setParameter("valor4", nombre);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean inserta_id_dpt_ps(int id_dpt_ps, String nm_dpt_ps) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO id_dpt_ps(id_dpt_ps, nm_dpt_ps, cd_dpt_cer, cod_negocio, porc_rec, porc_desemp, porc_bosol, qty_mrhrc_gp) VALUES (:valor1, :valor2, 1, 0, 0, 0, 0, 0);");
            query.setParameter("valor1", id_dpt_ps);
            query.setParameter("valor2", nm_dpt_ps);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            if (tx != null) {
                tx.rollback();
            }
            return false;
        }
        return true;
    }

    public boolean inserta_co_mrhrc_gp(int cd_mrhrc_gp, String nm_mrhrc_gp, int id_dpt_ps) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("INSERT INTO co_mrhrc_gp(cd_mrhrc_gp, nm_mrhrc_gp, id_dpt_ps, porc_rec) VALUES (:valor1, :valor2, :valor3, 0);");
            query.setParameter("valor1", cd_mrhrc_gp);
            query.setParameter("valor2", nm_mrhrc_gp);
            query.setParameter("valor3", id_dpt_ps);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            if (tx != null) {
                tx.rollback();
            }
            return false;
        }
        return true;
    }

    /**
     * ******************************************************************************************************************************************
     */
    /**
     * ****************************************************	ACTUALIZA
     * *********************************************************
     */
    /**
     * ******************************************************************************************************************************************
     */
    public boolean actualiza_estado_as_itm_load_file(int idFile) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true' WHERE ID_FILE= :valor1");
            query.setParameter("valor1", idFile);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_ejecutado_as_itm_load_file(int idFile) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true', EJECUTADO = 'true', VAR_PROCESADO = 1 WHERE ID_FILE= :valor1");
            query.setParameter("valor1", idFile);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_error_as_itm_load_file(int idFile) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true', EJECUTADO = 'true', VAR_PROCESADO = 2,FECHA = NOW() WHERE VAR_PROCESADO = 0 AND ID_FILE= :valor1 ");
            query.setParameter("valor1", idFile);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_error_as_itm_load_file(int idFile, String novedades) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true', EJECUTADO = 'true', VAR_PROCESADO = 2,FILE_VND =:param3, FECHA = NOW()  WHERE VAR_PROCESADO = 0 AND ID_FILE= :valor1 ");
            query.setParameter("valor1", idFile);
            query.setParameter("param3", novedades);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_info_as_item_load_file(int idFile, int total, int totalProcesados, int totalArchivo, String novedades) {
        log.info("" + idFile + " " + total + " " + totalProcesados);
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true', EJECUTADO = 'true', VAR_PROCESADO = 1, cnt_itm_ttl = :param1, cnt_itm_prc = :param2, file_vnd =:param3,  cnt_file_reg= :param4 WHERE ID_FILE = :valor1 ");
            query.setParameter("valor1", idFile);
            query.setParameter("param1", total);
            query.setParameter("param2", totalProcesados);
            query.setParameter("param3", novedades);
            query.setParameter("param4", totalArchivo);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_info_as_item_load_file_API(int idFile, int total, int totalProcesados, int totalArchivo, String novedades, String file) {
        log.info("" + idFile + " " + total + " " + totalProcesados);
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE SET ESTADO='true', EJECUTADO = 'true', VAR_PROCESADO = 1,FECHA = NOW(), cnt_itm_ttl = :param1, cnt_itm_prc = :param2, file_vnd =:param3,  cnt_file_reg= :param4, auditoria_file_name= :param5 WHERE ID_FILE = :valor1 ");
            query.setParameter("valor1", idFile);
            query.setParameter("param1", total);
            query.setParameter("param2", totalProcesados);
            query.setParameter("param3", novedades);
            query.setParameter("param4", totalArchivo);
            query.setParameter("param5", file);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_moblar(String de_itm, String de_descripcion, double alto, double ancho, double largo, String color, String diseniador, long id_itm, boolean reqArmado) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM SET DE_ITM = :valor1,de_descripcion = :valor2,ALTO = :valor3,ANCHO = :valor4,LARGO = :valor5,COLOR = :valor6,DISENIADOR = :valor7,fl_req_arm = :valor9  WHERE ID_ITM = :valor8 ");
            query.setParameter("valor1", de_itm.length() >= 149 ? de_itm.substring(0, 149) : de_itm);
            query.setParameter("valor2", de_descripcion);
            query.setParameter("valor3", alto);
            query.setParameter("valor4", ancho);
            query.setParameter("valor5", largo);
            query.setParameter("valor6", color);
            query.setParameter("valor7", diseniador);
            query.setParameter("valor8", id_itm);
            query.setParameter("valor9", reqArmado);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error("error bolcado " + id_itm);
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_api(String nm_itm, String de_itm, String de_descripcion,
            Double alto, Double ancho, Double largo,
            String color, String diseniador, boolean reqArmado,
            Long ID_SPR, String edad, Double peso, String genero,
            String cd_itm, Boolean fl_rp_rq, Integer fl_wm_rq, Integer id_mrhrc_gp, Long id_itm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();

            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM SET DE_ITM = :valor1, de_descripcion = :valor2,ALTO = :valor3,ANCHO = :valor4,"
                    + "LARGO = :valor5,COLOR = :valor6,DISENIADOR = :valor7,fl_req_arm = :valor8, ID_SPR =:valor9, EDAD =:valor10, PESO = :valor11,"
                    + "GENERO = :valor12, CD_ITM = :valor13, FL_RP_RQ = :valor15, "
                    + "FL_WM_RQ = :valor16, ID_TY_ITM = 0, FLAG_EC = 1, ID_MRHRC_GP = :valor17, NM_ITM= :valor18 "
                    + "WHERE ID_ITM = :valor0 ");
            query.setParameter("valor0", id_itm);
            query.setParameter("valor1", de_itm.length() >= 149 ? de_itm.substring(0, 149) : de_itm);
            query.setParameter("valor2", de_descripcion);
            query.setParameter("valor3", alto);
            query.setParameter("valor4", ancho);
            query.setParameter("valor5", largo);
            query.setParameter("valor6", color);
            query.setParameter("valor7", diseniador);
            query.setParameter("valor8", reqArmado);
            query.setParameter("valor9", ID_SPR);
            query.setParameter("valor10", edad);
            query.setParameter("valor11", peso);
            query.setParameter("valor12", genero);
            query.setParameter("valor13", cd_itm);
            query.setParameter("valor15", fl_rp_rq);
            query.setParameter("valor16", fl_wm_rq);
            query.setParameter("valor17", id_mrhrc_gp);
            query.setParameter("valor18", nm_itm.length() >= 149 ? nm_itm.substring(0, 149) : nm_itm);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error("error bolcado " + id_itm);
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*public boolean actualiza_as_itm_str_moblar(int fl_promocion, String prm_dsc_lbl,long ID_ITM,long id_bsn_un) {
		Transaction tx = null;
		try {
			tx = this.sessionArtsEc.beginTransaction();
			Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET fl_promocion = :valor1, prm_dsc_lbl = :valor2 WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");
			query.setParameter("valor1", fl_promocion);
			query.setParameter("valor2", prm_dsc_lbl);
			query.setParameter("valor5", ID_ITM);
			query.setParameter("valor6", id_bsn_un);
			
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}*/
    public boolean actualiza_as_itm_str_moblar(int fl_promocion, String prm_dsc_lbl, long ID_ITM, long id_bsn_un, Date prm_dtf, Date prm_dtt, double prm_prc) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query;
            if (fl_promocion == 1) {
                query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET fl_promocion = :valor1, prm_dsc_lbl = :valor2, prm_dtf= :valor7, prm_dtt= :valor8, fec_prc_ecom = :valor9, prm_prc = :valor3 WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");
                query.setParameter("valor1", fl_promocion);
                query.setParameter("valor2", prm_dsc_lbl);
                query.setParameter("valor3", prm_prc);
                query.setParameter("valor5", ID_ITM);
                query.setParameter("valor6", id_bsn_un);
                query.setTimestamp("valor7", prm_dtf);
                query.setTimestamp("valor8", prm_dtt);
                query.setTimestamp("valor9", new Date());
            } else {
                query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET fl_promocion = :valor1, prm_dsc_lbl = :valor2, prm_dtf= :valor7, prm_dtt= :valor8, fec_prc_ecom = :valor9, prm_prc = :valor3 WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");

                query.setParameter("valor1", fl_promocion);
                query.setParameter("valor2", null);
                query.setParameter("valor3", 0.0);
                query.setParameter("valor5", ID_ITM);
                query.setParameter("valor6", id_bsn_un);
                query.setTimestamp("valor7", null);
                query.setTimestamp("valor8", null);
                query.setTimestamp("valor9", new Date());
            }
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /**
     * @param SLS_PRC
     * @param TX_A
     * @param ID_ITM
     * @param MARCA
     * @param id_bsn_un
     * @param stock
     * @param fl_promocion
     * @param prm_dsc_lbl
     * @param prm_prc
     * @param prm_dtf
     * @param prm_dtt
     * @return
     */
    public boolean actualiza_as_itm_str_api(float SLS_PRC, int TX_A, String MARCA, Float stock, int fl_promocion, String prm_dsc_lbl, Date prm_dtf, Date prm_dtt, double prm_prc, long ID_ITM, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET "
                    + "SLS_PRC = :valor1, TX_A = :valor2, MARCA = :valor3, "
                    + "FEC_PRC_DAT =:valor5, FEC_PRC_STOCK =:valor6, "
                    + "STOCK =:valor7, fl_promocion = :valor8, prm_dsc_lbl = :valor9, "
                    + "prm_dtf= :valor10, prm_dtt= :valor11, fec_prc_ecom = :valor12, "
                    + "prm_prc = :valor13 WHERE ID_ITM = :valor14 AND ID_BSN_UN = :valor15 ");
            query.setParameter("valor1", SLS_PRC);
            query.setParameter("valor2", TX_A);
            query.setParameter("valor3", MARCA);
            query.setTimestamp("valor5", new Date());
            query.setTimestamp("valor6", new Date());
            query.setParameter("valor7", stock);
            query.setParameter("valor8", fl_promocion);
            query.setParameter("valor9", prm_dsc_lbl);
            query.setTimestamp("valor10", prm_dtf);
            query.setTimestamp("valor11", prm_dtt);
            if (fl_promocion == 1) {
                query.setTimestamp("valor12", new Date());
            } else {
                query.setTimestamp("valor12", null);
            }
            query.setParameter("valor13", prm_prc);
            query.setParameter("valor14", ID_ITM);
            query.setParameter("valor15", id_bsn_un);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*public boolean actualiza_as_itm_str_jugeton(int fl_promocion, String prm_dsc_lbl,long ID_ITM,long id_bsn_un, double prm_prc) {
		Transaction tx = null;
		try {
			tx = this.sessionArtsEc.beginTransaction();
			Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET fl_promocion = :valor1, prm_dsc_lbl = :valor2, prm_prc = :valor3  WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");
			query.setParameter("valor1", fl_promocion);
			query.setParameter("valor2", prm_dsc_lbl);
			query.setParameter("valor3", prm_prc);
			query.setParameter("valor5", ID_ITM);
			query.setParameter("valor6", id_bsn_un);
			
			
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}*/
    public boolean actualiza_as_itm_str_jugeton(int fl_promocion, String prm_dsc_lbl, long ID_ITM, long id_bsn_un, double prm_prc, Date prm_dtf, Date prm_dtt) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET fl_promocion = :valor1, prm_dsc_lbl = :valor2, prm_prc = :valor3, prm_dtf= :valor7, prm_dtt= :valor8,FEC_PRC_ECOM =:valor9  WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");
            query.setParameter("valor1", fl_promocion);
            if (fl_promocion == 0) {
                query.setParameter("valor2", null);
                query.setParameter("valor3", 0.0);
                query.setTimestamp("valor7", null);
                query.setTimestamp("valor8", null);
            } else {
                query.setParameter("valor2", prm_dsc_lbl);
                query.setParameter("valor3", prm_prc);
                query.setTimestamp("valor7", prm_dtf);
                query.setTimestamp("valor8", prm_dtt);
            }
            query.setParameter("valor5", ID_ITM);
            query.setParameter("valor6", id_bsn_un);
            query.setTimestamp("valor9", new Date());

            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_imagen(long id_itm_imagen, long id_itm, String imagen) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_IMAGEN SET IMAGEN = :valor1 , ESTADO = 1 WHERE ID_ITM_IMAGEN = :valor2 AND ID_ITM = :valor3");
            query.setParameter("valor1", imagen);
            query.setParameter("valor2", id_itm_imagen);
            query.setParameter("valor3", id_itm);
            //log.info("Query:actualiza_as_itm_imagen; - " + query.getQueryString());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_imagen1(long id_itm_imagen, long id_itm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_IMAGEN SET ESTADO = 1 WHERE ID_ITM_IMAGEN = :valor2 AND ID_ITM = :valor3");
            query.setParameter("valor2", id_itm_imagen);
            query.setParameter("valor3", id_itm);
            //log.info("Query:actualiza_as_itm_imagen; - " + query.getQueryString());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*public boolean actualiza_as_itm_imagen(long id_itm_imagen,long id_itm,String imagen, String imagenSm) {
		Transaction tx = null;
		try {
			tx = this.sessionArtsEc.beginTransaction();
			Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_IMAGEN SET IMAGEN = :valor1, IMAGEN_SM = :valor4 , ESTADO = 1 WHERE ID_ITM_IMAGEN = :valor2 AND ID_ITM = :valor3");
			query.setParameter("valor1", imagen);
			query.setParameter("valor2", id_itm_imagen);
			query.setParameter("valor3", id_itm);
			query.setParameter("valor4", imagenSm);
			//log.info("Query:actualiza_as_itm_imagen; - " + query.getQueryString());
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}*/
    public boolean actualiza_as_itm_gnr(long ID_SPR, String DE_ITM, String de_descripcion, String edad, double alto, double ancho, double largo, double peso, String genero, long id_itm, double peso_serv) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM SET ID_SPR = :valor1, DE_ITM = :valor2, de_descripcion = :valor3, "
                    + "EDAD = :valor4, ALTO = :valor5, ANCHO = :valor6, LARGO = :valor7, PESO = :valor8, GENERO = :valor9, PESO_SERVIENTREGA = :valor11 WHERE ID_ITM = :valor10 ");
            query.setParameter("valor1", ID_SPR);
            query.setParameter("valor2", DE_ITM);
            query.setParameter("valor3", de_descripcion);
            query.setParameter("valor4", edad);
            query.setParameter("valor5", alto);
            query.setParameter("valor6", ancho);
            query.setParameter("valor7", largo);
            query.setParameter("valor8", peso);
            query.setParameter("valor9", genero);
            query.setParameter("valor10", id_itm);
            query.setParameter("valor11", peso_serv);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm(String cd_itm, String nm_itm, boolean fl_rp_rq, int fl_wm_rq, long ID_ITM) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM SET CD_ITM = :valor1, NM_ITM = :valor2, FL_RP_RQ = :valor3, FL_WM_RQ = :valor4, ID_TY_ITM = 0, FLAG_EC = 1 WHERE ID_ITM = :valor5 ");
            query.setParameter("valor1", cd_itm);
            query.setParameter("valor2", nm_itm.length() >= 149 ? nm_itm.substring(0, 149) : nm_itm);///
            query.setParameter("valor3", fl_rp_rq);
            query.setParameter("valor4", fl_wm_rq);
            query.setParameter("valor5", ID_ITM);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_mrhrc(String cd_itm, String nm_itm, long ID_MRHRC_GP, boolean fl_rp_rq, int fl_wm_rq, long ID_ITM, String DE_ITM) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM SET CD_ITM = :valor1, NM_ITM = :valor2, ID_MRHRC_GP = :valor3, FL_RP_RQ = :valor4, FL_WM_RQ = :valor5, ID_TY_ITM = 0, FLAG_EC = 1, DT_UPDATE_CODIPDV = NOW(), DE_ITM= :valor7  WHERE ID_ITM = :valor6 ");
            query.setParameter("valor1", cd_itm);
            query.setParameter("valor2", nm_itm);
            query.setParameter("valor3", ID_MRHRC_GP);
            query.setParameter("valor4", fl_rp_rq);
            query.setParameter("valor5", fl_wm_rq);
            query.setParameter("valor6", ID_ITM);
            query.setParameter("valor7", DE_ITM);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_id_ps(String ID_ITM_PS, long id_itm, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE ID_PS SET ID_ITM_PS = :valor1, PRTY = 'P' WHERE ID_ITM = :valor2 AND ID_BSN_UN = :valor3 ");
            query.setParameter("valor1", ID_ITM_PS);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", id_bsn_un);

            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*aqui*/
    public boolean actualiza_as_itm_str(float SLS_PRC, int TX_A, String MARCA, String PRESENTACION, long ID_ITM, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET SLS_PRC = :valor1, TX_A = :valor2, MARCA = :valor3, PRESENTACION = :valor4, FEC_PRC_DAT =:valor7 WHERE ID_ITM = :valor5 AND ID_BSN_UN = :valor6 ");
            query.setParameter("valor1", SLS_PRC);
            query.setParameter("valor2", TX_A);
            query.setParameter("valor3", MARCA);
            query.setParameter("valor4", PRESENTACION);
            query.setParameter("valor5", ID_ITM);
            query.setParameter("valor6", id_bsn_un);
            query.setTimestamp("valor7", new Date());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_pa_str_rtl(long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE pa_str_rtl SET fl_stock_load = true where id_bsn_un = :valor1 ");
            query.setParameter("valor1", id_bsn_un);

            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_estado_itm_load_file_stock_error(int id_file) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE_STOCK SET PROCESADO='true',ERROR='true' WHERE ID_FILE= :valor1 ");
            query.setParameter("valor1", id_file);

            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_info_as_item_load_file_stock(int idFile, int total, int totalProcesados, int totalArchivo, String novedades) {
        log.info("" + idFile + " " + total + " " + totalProcesados);
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE_STOCK SET cnt_itm_ttl = :param1, cnt_itm_prc = :param2, file_vnd =:param3,  cnt_file_reg= :param4 WHERE ID_FILE = :valor1 ");
            query.setParameter("valor1", idFile);
            query.setParameter("param1", total);
            query.setParameter("param2", totalProcesados);
            query.setParameter("param3", novedades);
            query.setParameter("param4", totalArchivo);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_estado_itm_load_file_stock_ok(int id_file) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_LOAD_FILE_STOCK SET PROCESADO='true' WHERE ID_FILE= :valor1 ");
            query.setParameter("valor1", id_file);

            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_str_stock(int stock, long id_itm, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET STOCK = :valor1, FEC_PRC_STOCK = :valor4 WHERE ID_ITM = :valor2 AND ID_BSN_UN = :valor3 ");
            query.setParameter("valor1", stock);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", id_bsn_un);
            query.setTimestamp("valor4", new Date());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_as_itm_str_gnr(String marca, int fl_promocion, String prm_dsc_lbl, long id_itm, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET MARCA = :valor1,fl_promocion = :valor4, prm_dsc_lbl = :valor5  WHERE ID_ITM = :valor2  AND ID_BSN_UN = :valor3 ");
            query.setParameter("valor1", marca);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", id_bsn_un);
            query.setParameter("valor4", fl_promocion);
            query.setParameter("valor5", prm_dsc_lbl);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /*public boolean actualiza_as_itm_str_gnr_jugeton(String marca,int fl_promocion, String prm_dsc_lbl,long id_itm,	long id_bsn_un, double prm_prc) {
		Transaction tx = null;
		try {
			tx = this.sessionArtsEc.beginTransaction();
			Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET MARCA = :valor1,fl_promocion = :valor4, prm_dsc_lbl = :valor5, prm_prc = :valor6 WHERE ID_ITM = :valor2  AND ID_BSN_UN = :valor3 ");
			query.setParameter("valor1", marca);
			query.setParameter("valor2", id_itm);
			query.setParameter("valor3", id_bsn_un);
			query.setParameter("valor4", fl_promocion);
			query.setParameter("valor5", prm_dsc_lbl);
			query.setParameter("valor6", prm_prc);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}*/
    public boolean actualiza_as_itm_str_gnr_jugeton(String marca, int fl_promocion, String prm_dsc_lbl, long id_itm, long id_bsn_un, double prm_prc, Date prm_dtf, Date prm_dtt) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET MARCA = :valor1,fl_promocion = :valor4, prm_dsc_lbl = :valor5, prm_prc = :valor6, prm_dtf= :valor7, prm_dtt= :valor8, fec_prc_ecom = :valor9 WHERE ID_ITM = :valor2  AND ID_BSN_UN = :valor3 ");

            if (fl_promocion == 0) {
                query.setParameter("valor5", null);
                query.setParameter("valor6", 0.0);
                query.setTimestamp("valor7", null);
                query.setTimestamp("valor8", null);
            } else {
                query.setParameter("valor5", prm_dsc_lbl);
                query.setParameter("valor6", prm_prc);
                query.setTimestamp("valor7", prm_dtf);
                query.setTimestamp("valor8", prm_dtt);
            }

            query.setParameter("valor1", marca);
            query.setParameter("valor2", id_itm);
            query.setParameter("valor3", id_bsn_un);
            query.setParameter("valor4", fl_promocion);
            query.setTimestamp("valor9", new Date());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public boolean actualiza_ecommerce_catalogo_items_estado(Integer idListCatalogo, Integer idCatalogo, Long idItm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE ecommerce_catalogo_items SET estado = 1 WHERE id_listado_catalogo = :valor1 AND id_catalogo = :valor2  AND id_itm = :valor3 ");
            query.setParameter("valor1", idListCatalogo);
            query.setParameter("valor2", idCatalogo);
            query.setParameter("valor3", idItm);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    /**
     * ******************************************************************************************************************************************
     */
    /**
     * ****************************************************	DELETE
     * *********************************************************
     */
    /**
     * ******************************************************************************************************************************************
     */
    public boolean borrar_ecommerce_catalogo_items(Integer idListCatalogo, Integer idCatalogo, Long idItm) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("DELETE FROM ecommerce_catalogo_items WHERE id_itm = :valor3 AND id_listado_catalogo = :valor1 AND id_catalogo <> :valor2 AND cast(fecha as date) != CURRENT_DATE");
            query.setParameter("valor1", idListCatalogo);
            query.setParameter("valor2", idCatalogo);
            query.setParameter("valor3", idItm);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    //Consultas base ecomerce
    public List<ItemTmp> consulta_as_itm_tmp_by_id_bsn_un(Integer id_bsn_un) {
        try {
            Query query = sessionArtsEc.createQuery("FROM com.allc.arms.server.persistence.item.ItemTmp  WHERE idBsnUn = " + id_bsn_un + " and fechaDesde < now() and fechaHasta > now() ");
            log.info(query.getQueryString());
            return query.list();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return null;
        }
    }

    public List<ItemTmp> consulta_all_as_itm_tmp() {
        try {
            Query query = sessionArtsEc.createQuery("FROM com.allc.arms.server.persistence.item.ItemTmp WHERE fechaDesde <= to_date(cast(now()as text),'YYYY-MM-DD') and fechaHasta >= to_date(cast(now()as text),'YYYY-MM-DD') ");
            log.info(query.getQueryString());
            return query.list();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return null;
        }
    }

    public boolean update_as_itm_str_stock_tmp(long id_itm, long id_bsn_un, float STOCK) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET "
                    + "STOCK =:valor3 WHERE ID_ITM = :valor1 AND ID_BSN_UN = :valor2 ");
            query.setParameter("valor1", id_itm);
            query.setParameter("valor2", id_bsn_un);
            query.setParameter("valor3", STOCK);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    public Integer consulta_id_bsn_un_x_tienda(long cd_str_rt) {
        try {
            //id_bsn_un, de_str_rt, cd_str_rt, iva_tax, inc_prc, imp_1, imp_2, imp_3, imp_4, imp_5, imp_6, imp_7, imp_8, ce_cobe, no_afil_fl, id_ctab, dist_dir, fl_stock_load 
            Query query = this.sessionArtsEc.createSQLQuery("select id_bsn_un FROM PA_STR_RTL where cd_str_rt = " + cd_str_rt);
            return (Integer) query.uniqueResult();  //Rows AS_ITM_LOAD_FILE
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public List<Object[]> consulta_file_as_itm_load_file() {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT id_file, descripcion, estado, ejecutado, fecha, auditoria_file_name FROM AS_ITM_LOAD_FILE WHERE estado is true and ejecutado is true and rvw_ecom is false and date(fecha) = date(now())");
            return (List<Object[]>) query.list();
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Object[] consulta_as_itm_ecomm(Session session, String cdItm, int id_bsn_un) {
        try {
            Query query = session.createSQLQuery("SELECT i.id_itm ,nm_itm, s.sls_prc, i.de_descripcion, i.de_itm, alto, largo, ancho, peso, stock  FROM AS_ITM i inner join AS_ITM_STR s on i.id_itm = s.id_itm WHERE ltrim(i.cd_itm,'0') = ltrim('" + cdItm + "','0') and s.id_bsn_un = " + id_bsn_un + "order by i.id_itm limit 1");
            return (Object[]) query.uniqueResult();  //Rows AS_ITM_LOAD_FILE
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public boolean update_as_itm_load_file(Long idFile) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("update AS_ITM_LOAD_FILE set rvw_ecom = true where id_file =:valor1 ");
            query.setParameter("valor1", idFile);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }
}
