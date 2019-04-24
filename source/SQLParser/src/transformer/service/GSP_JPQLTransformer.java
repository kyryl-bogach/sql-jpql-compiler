package transformer.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import elements.CampoFrom;
import elements.CampoSelect;
import elements.Condicion;
import elements.FuncionParser;
import elements.Having;
import elements.Join;
import elements.TipoJoin;
import javafx.util.Pair;
import transformer.api.GSP_API;
import transformer.factory.JPQLTransformerFactory;
import transformer.factory.JPQLTransformers;
import utils.Utils;
import utils.introspection.ClaseJPA;
import utils.introspection.ClaseJPA.AtributoClaseJPA;
import utils.introspection.ClassIntrospection;

// (?) Sutituir javafx.util.Pair por 
// https://stackoverflow.com/questions/521171/a-java-collection-of-value-pairs-tuples
// AbstractMap.SimpleEntry

public class GSP_JPQLTransformer extends JPQLTransformerBase {

	private static final char CARACTER_AUTOINCREMENTAL = 'a';
	private static final String DIFERENCIADOR = "_";
	private String diferenciador = "";
	private char aliasAutoIncrementado;

	// Variables del FROM
	private TreeMap<String, ClaseJPA> mappingAliasClase;
	private TreeMap<String, String> mappingNombreBDAlias;
	private HashMap<String, ClaseJPA> mappingClasesNombreJPA;
	private LinkedList<String> fromEncontrados;

	// Variables de JOINS
	private LinkedList<String> joinEncontrados;

	// Variables del SELECT
	private LinkedList<String> selectEncontrados;

	// Variables del WHERE
	private LinkedList<String> whereEncontrados;

	// Variables del GROUP BY
	private LinkedList<String> groupByEncontrados;

	// Variables del HAVING
	private LinkedList<String> havingEncontrados;

	// Variables del ORDER BY
	private LinkedList<String> orderByEncontrados;

	public GSP_JPQLTransformer() {
		super();
		this.api = new GSP_API();

		this.aliasAutoIncrementado = CARACTER_AUTOINCREMENTAL;
		this.mappingAliasClase = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.mappingNombreBDAlias = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.mappingClasesNombreJPA = new HashMap<>();

		// Lista con todo lo incluido (separamos luego por ',')
		this.fromEncontrados = new LinkedList<>();
		this.joinEncontrados = new LinkedList<>();
		this.selectEncontrados = new LinkedList<>();
		this.whereEncontrados = new LinkedList<>();
		this.groupByEncontrados = new LinkedList<>();
		this.havingEncontrados = new LinkedList<>();
		this.orderByEncontrados = new LinkedList<>();
	}

	public void reset() {
		this.diferenciador = "";
		this.aliasAutoIncrementado = CARACTER_AUTOINCREMENTAL;

		this.mappingAliasClase.clear();
		this.mappingNombreBDAlias.clear();
		this.mappingClasesNombreJPA.clear();
		this.fromEncontrados.clear();
		this.joinEncontrados.clear();
		this.selectEncontrados.clear();
		this.whereEncontrados.clear();
		this.groupByEncontrados.clear();
		this.havingEncontrados.clear();
		this.orderByEncontrados.clear();

		this.api.reset();
	}

	@Override
	public String transform(String sql) {
		this.reset();
		this.api.parse(sql);

		this.select = this.api.getSelect();
		this.from = this.api.getFrom();
		this.where = this.api.getWhere();
		this.groupBy = this.api.getGroupBy();
		this.orderBy = this.api.getOrderBy();

		String selectQuery = "SELECT ";
		String fromQuery = "FROM ";
		String whereQuery = "";
		String groupByQuery = "";
		String havingByQuery = "";
		String orderByQuery = "";

		// Miramos si usamos diferenciador o no
		this.checkUsoLimitador();

		// Mapeamos y construimos los datos
		this.buildFrom();
		this.buildJoin();
		this.buildSelect();
		this.buildWhere();
		this.buildGroupBy();
		this.buildOrderBy();

		// FROM
		fromQuery += String.join(", ", fromEncontrados) + " ";

		// JOIN
		if (!joinEncontrados.isEmpty()) {
			fromQuery += String.join("", joinEncontrados);
		}

		// SELECT
		selectQuery += String.join(", ", selectEncontrados) + " ";

		// WHERE
		if (!whereEncontrados.isEmpty()) {
			whereQuery += "WHERE " + String.join(" ", whereEncontrados) + " ";
		}

		// GROUP BY
		if (!groupByEncontrados.isEmpty()) {
			groupByQuery += "GROUP BY " + String.join(", ", groupByEncontrados) + " ";
		}

		// HAVING
		if (!havingEncontrados.isEmpty()) {
			havingByQuery += "HAVING " + String.join(" ", havingEncontrados) + " ";
		}

		// ORDER BY
		if (!orderByEncontrados.isEmpty()) {
			orderByQuery += "ORDER BY " + String.join(", ", orderByEncontrados) + " ";
		}

		return (selectQuery + fromQuery + whereQuery + groupByQuery + havingByQuery + orderByQuery).trim();
	}

	private void buildFrom() {
		for (CampoFrom campoFrom : this.from.getTables()) {
			String nombreCampo = campoFrom.getNombre(); // FROM (Table) a
			String aliasCampo = campoFrom.getAlias(); // FROM Table (a)

			ClaseJPA claseJPA = ClassIntrospection.getClaseJPA(nombreCampo);

			String fromString = claseJPA.getNombreJPA();
			String aliasUsado = "";

			if (aliasCampo == null || aliasCampo.equals("")) {
				// Si no tiene alias, se lo asignamos nosotros
				aliasUsado = aliasAutoIncrementado + this.diferenciador;
				fromString += " " + aliasUsado;
				this.aliasAutoIncrementado++;
			} else {
				// Si tiene alias, lo respetamos
				aliasUsado = aliasCampo;
				fromString += " " + aliasUsado;
			}

			this.mappingAliasClase.put(aliasUsado, claseJPA);
			this.mappingNombreBDAlias.put(nombreCampo, aliasUsado);
			this.mappingClasesNombreJPA.put(claseJPA.getNombreJPA(), claseJPA);
			this.fromEncontrados.add(fromString);
		}
	}

	public void buildJoin() {
		LinkedList<Join> joins = this.from.getJoins();

		for (Join join : joins) {
			String aliasCampo = join.getTable().getAlias();
			String nombreCampo = join.getTable().getNombre();

			ClaseJPA claseJPA = ClassIntrospection.getClaseJPA(nombreCampo);
			String aliasUsado = "";

			if (aliasCampo == null || aliasCampo.equals("")) {
				// Si no tiene alias, se lo asignamos nosotros
				aliasUsado = aliasAutoIncrementado + this.diferenciador;
				this.aliasAutoIncrementado++;
			} else {
				// Si tiene alias, lo respetamos
				aliasUsado = aliasCampo;
			}

			// A�adimos los joins al mapping
			this.mappingAliasClase.put(aliasUsado, claseJPA);
			this.mappingNombreBDAlias.put(nombreCampo, aliasUsado);
			this.mappingClasesNombreJPA.put(claseJPA.getNombreJPA(), claseJPA);

			// https://www.objectdb.com/java/jpa/query/jpql/from
			Set<Condicion> condiciones = join.getCondiciones();

			// Buscamos una relaci�n y mapeo por '.'
			claseJPA.getAtributoWithJoinColumn(nombreCampo);

			// Buscamos la relaci�n concreta no presente en los atributos
			// sino por objetos JPA
			Pair<String, Condicion> relacionObjetual = this.getRelationCondicition(join, claseJPA);
			if (relacionObjetual != null) {
				// Falta ver las dem�s condiciones JOIN p.category c ON ...
				condiciones.remove(relacionObjetual.getValue());
				String onCondition = parseCondicionExpressions(condiciones, join.getCondicionRaw());
				onCondition = Utils.reemplazarYNormalizar(onCondition, relacionObjetual.getValue().getRawConditionRE());

				if (!onCondition.isEmpty()) {
					onCondition = "ON " + onCondition;
				}

				joinEncontrados.add(relacionObjetual.getKey() + " " + onCondition);
			} else {
				// Construimos la condici�n y la query
				String rawWhereString = join.getCondicionRaw();
				String treatedJoinString = parseCondicionExpressions(condiciones, rawWhereString);

				// Tenemos como enumerados: JOIN, LEFT, RIGHT ..., filtramos JOIN para no
				// volverlo a poner
				// Para los dem�s necesitamos la sintasis JOIN
				String tipoJoinString = getTipoJoinString(join.getTipo());
				joinEncontrados.add(
						tipoJoinString + " " + claseJPA.getNombreJPA() + " " + aliasUsado + " ON " + treatedJoinString);
			}
		}
	}

	private void buildSelect() {
		Collection<CampoSelect> selectFields = this.select.getFields();
		for (CampoSelect campoSelect : selectFields) {

			String aliasCampo = campoSelect.getAlias(); // customerId as (identificador)
			// String nombreCampo = campoSelect.getNombre(); // (customerId)
			String tablaReferida = campoSelect.getTableReference(); // (c).customerId
			String nombreRaw = campoSelect.getRawNombre(); // c.(customerId)

			String selectFormado = "";

			if (nombreRaw.equals("*")) {
				if (Utils.isEmpty(tablaReferida)) {
					// SELECT * FROM T1, T2 --> SELECT a, b FROM T1 a, T2 b
					// SELECT * FROM T1 --> SELECT a FROM T1 a
					selectFormado += String.join(", ", this.mappingAliasClase.keySet());
				} else {
					// SELECT a.* FROM T1 a, T2 b --> SELECT a FROM T1 a, T2 b
					if (this.mappingAliasClase.containsKey(tablaReferida)) {
						// SELECT c.* FROM Customers c
						selectFormado += tablaReferida;
					} else {
						// SELECT customers.* FROM Customers
						selectFormado += this.mappingNombreBDAlias.get(tablaReferida);
					}
				}

				this.selectEncontrados.add(selectFormado);
				continue;
			}

			/*
			 * Hay que buscar en todas las tablas del FROM el campo (tiene que estar en
			 * alguno de los dos, sino habr�a colisi�n y la query estar�a mal).
			 * 
			 * Recorremos, pues, todas las clases del FROM:
			 */

			String campoParseado = this.parseCondition(nombreRaw, aliasCampo);
			this.selectEncontrados.add(campoParseado);
		}
	}

	private void buildWhere() {
		String rawWhereString = this.where.getCondicionRaw();
		Set<Condicion> condiciones = this.where.getCondiciones();
		if (condiciones == null || condiciones.isEmpty()) {
			return;
		}

		String treatedWhereString = parseCondicionExpressions(condiciones, rawWhereString);
		this.whereEncontrados.add(treatedWhereString);
	}

	private void buildGroupBy() {
		Collection<CampoSelect> groupByCampos = this.groupBy.getFields();
		if (groupByCampos.isEmpty()) {
			return;
		}

		for (CampoSelect campo : groupByCampos) {
			String condicion = this.parseCondition(campo.getRawNombre());
			this.groupByEncontrados.add(condicion);
		}

		Having having = this.groupBy.getHaving();
		if (having == null) {
			return;
		}

		String rawWhereString = having.getCondicionRaw();
		Set<Condicion> condiciones = having.getCondiciones();
		if (condiciones == null || condiciones.isEmpty()) {
			return;
		}

		String treatedWhereString = parseCondicionExpressions(condiciones, rawWhereString);
		this.havingEncontrados.add(treatedWhereString);

	}

	private void buildOrderBy() {
		Collection<CampoSelect> orderByCampos = this.orderBy.getFields();
		if (orderByCampos.isEmpty()) {
			return;
		}

		for (CampoSelect campo : orderByCampos) {
			String condicion = this.parseCondition(campo.getRawNombre());
			String direccionOrden = campo.getAlias(); // DESC, ASC
			if (!Utils.isEmpty(direccionOrden)) {
				condicion += " " + direccionOrden.toUpperCase();
			}

			this.orderByEncontrados.add(condicion);
		}
	}

	// El par�metro claseFrom es la clase a la que se le est� haciendo el JOIN
	private Pair<String, Condicion> getRelationCondicition(Join join, ClaseJPA claseFrom) {
		Set<Condicion> condiciones = join.getCondiciones();
		// System.out.println("condiciones: " + condiciones);

		for (Condicion condicion : condiciones) {
			String condicionParseadaIzquierda = this.parseCondition(condicion.getIzquierda());
			String condicionParseadaDerecha = this.parseCondition(condicion.getDerecha());
			String referenciaIzquierda = Utils.getOnlyFieldTable(condicionParseadaIzquierda);
			String referenciaDerecha = Utils.getOnlyFieldTable(condicionParseadaDerecha);
			String campoIzquierda = Utils.getRawColumnValue(condicionParseadaIzquierda);
			String campoDerecha = Utils.getRawColumnValue(condicionParseadaDerecha);

			String campoValor;
			String referenciaNull;
			String refecenciaValor;

			// System.out.println("campoIzquierda: " + campoIzquierda);
			// System.out.println("campoDerecha: " + campoDerecha);
			// System.out.println("referenciaIzquierda: " + referenciaIzquierda);
			// System.out.println("referenciaDerecha: " + referenciaDerecha);

			if (Utils.isEmpty(campoIzquierda)) {
				campoValor = campoDerecha;
				referenciaNull = referenciaIzquierda;
				refecenciaValor = referenciaDerecha;
			} else if (Utils.isEmpty(campoDerecha)) {
				campoValor = campoIzquierda;
				referenciaNull = referenciaDerecha;
				refecenciaValor = referenciaIzquierda;
			} else if (Utils.isEmpty(referenciaIzquierda) || Utils.isEmpty(referenciaDerecha)) {
				continue;
			} else if (this.mappingAliasClase.get(referenciaIzquierda).equals(claseFrom)) {
				campoValor = campoIzquierda;
				referenciaNull = referenciaDerecha;
				refecenciaValor = referenciaIzquierda;
			} else if (this.mappingAliasClase.get(referenciaDerecha).equals(claseFrom)) {
				campoValor = campoDerecha;
				referenciaNull = referenciaIzquierda;
				refecenciaValor = referenciaDerecha;
			} else {
				continue;
			}

			// Analizamos @OneToMany @ManyToMany @ManyToOne
			// Si encontramos uno de estos casos, lo devolvemos
			ClaseJPA claseValor = this.mappingAliasClase.get(refecenciaValor);
			ClaseJPA claseNull = this.mappingAliasClase.get(referenciaNull);

			if (claseValor.getAtributoID().equalsIgnoreCase(campoValor)) {
				AtributoClaseJPA atributoNull = claseNull.getAtributoWithJoinColumn(claseValor.getAtributoID());

				if (atributoNull != null && atributoNull.getManyToOne() != null) {
					if (claseValor.getClaseReferenciada().equals(atributoNull.getTipo())) {
						AtributoClaseJPA atributoValor = claseValor
								.getAtributoMappedByOneToMany(atributoNull.getNombre());

						String tipoJoinString = getTipoJoinString(join.getTipo());
						String joinResult = tipoJoinString + " ";

						if (claseFrom.equals(claseNull)) {
							joinResult += refecenciaValor + "." + atributoValor.getNombre() + " " + referenciaNull;
						} else {
							joinResult += referenciaNull + "." + atributoNull.getNombre() + " " + refecenciaValor;
						}
						return new Pair<String, Condicion>(joinResult, condicion);
					}
				}
			}

			// Analizamos @OneToOne
			// Si encontramos el caso, lo devolvemos
			ClaseJPA claseIzq = this.mappingAliasClase.get(referenciaIzquierda);
			ClaseJPA claseDer = this.mappingAliasClase.get(referenciaDerecha);
			if (claseIzq == null || claseDer == null) {
				continue;
			}
			String tipoJoinString = getTipoJoinString(join.getTipo());
			AtributoClaseJPA atbIzq = claseIzq.getAtributoOneToOne(campoIzquierda);
			AtributoClaseJPA atbDer = claseDer.getAtributoOneToOne(campoDerecha);

//			System.out.println("referenciaIzquierda: " + referenciaIzquierda);
//			System.out.println("referenciaDerecha: " + referenciaDerecha);
//			System.out.println("claseIzq: " + claseIzq);
//			System.out.println("claseDer: " + claseDer);
//			System.out.println("campoIzquierda: " + campoIzquierda);
//			System.out.println("campoDerecha: " + campoDerecha);
//			System.out.println("atbIzq: " + atbIzq);
//			System.out.println("atbDer: " + atbDer);

			AtributoClaseJPA atbReferenciado = atbIzq != null ? atbIzq : atbDer;
			if (atbReferenciado.getOneToOne() == null) {
				continue;
			}

			ClaseJPA claseReferenciada;
			String tabla;
			String alias;
			if (claseFrom.equals(claseDer)) {
				claseReferenciada = claseIzq;
				tabla = referenciaIzquierda;
				alias = referenciaDerecha;
			} else if (claseFrom.equals(claseIzq)) {
				claseReferenciada = claseDer;
				tabla = referenciaDerecha;
				alias = referenciaIzquierda;
			} else {
				// Si no se encuentra el mapping, no se puede hacer y lo saltamos
				continue;
			}

			String referencia;
			String mappedBy = atbReferenciado.getOneToOne().mappedBy();

			if (!Utils.isEmpty(mappedBy)) {
				/*
				 * Si tenemos el propio mapping en la clase lo indicamos Al menos, una de las
				 * dos clases debe tener:
				 * 
				 * @OneToOne(mappedBy="")
				 */
				referencia = mappedBy;
			} else {
				// Sino, lo buscamos en la otra clase
				referencia = claseReferenciada.getAtributoMappedByOneToOne(atbReferenciado.getNombre()).getNombre();
			}

			String joinResult = tipoJoinString + " " + tabla + "." + referencia + " " + alias;
			return new Pair<String, Condicion>(joinResult, condicion);
		}

		// Si no se encutra, retornamos null
		return null;
	}

	private Pair<String, String> getTableFromAtributo(String atributoBuscado) {
		String atributoNormalizado = Utils.getJPAFormat(atributoBuscado);
		for (Map.Entry<String, ClaseJPA> claseBuscada : this.mappingAliasClase.entrySet()) {
			String alias = claseBuscada.getKey();
			ClaseJPA clase = claseBuscada.getValue();
			String atributoEnClase = clase.getAttributeRealName(atributoNormalizado);
			if (atributoEnClase != null) {
				return new Pair<>(alias, atributoEnClase);
			}
		}

		return null;
	}

	private Pair<String, String> getTableFromAtributo(ClaseJPA claseJPA, String atributoBuscado) {
		if (claseJPA == null || atributoBuscado == null) {
			return null;
		}

		String atributoNormalizado = Utils.getJPAFormat(atributoBuscado);
		String atributoEnClase = claseJPA.getAttributeRealName(atributoNormalizado);
		if (atributoEnClase != null) {
			return new Pair<>("", atributoEnClase);
		}

		return null;
	}

	private String parseCondition(String condicion) {
		String resultado = "";
		// System.out.println("condicion: " + condicion);

		if (condicion.contains(".")) {
			// Puede ser:
			// ... FROM TABLA a WHERE a.atr
			// o
			// ... FROM TABLA WHERE tabla.atr
			String referencia = Utils.getFieldTable(condicion);
			String rawColumn = Utils.getRawColumnValue(condicion);

			if (this.mappingNombreBDAlias.containsKey(referencia)) {
				// ... FROM TABLA WHERE tabla.atr
				String alias = this.mappingNombreBDAlias.get(referencia);
				ClaseJPA clase = this.mappingAliasClase.get(alias);
				String nombreCampo = clase.getAttributeRealName(rawColumn);

				resultado += alias + "." + nombreCampo;
			} else {
				// ... FROM TABLA a WHERE a.atr

				// Esto puede venir en may�sculas, min�sculas...etc
				// Como es case insensitive lo encontraremos, pero necesitamos el correcto
				ClaseJPA claseJPA = this.mappingAliasClase.get(referencia);
				referencia = this.getExactMappingAlias(referencia);

				Pair<String, String> aliasNombreReal = getTableFromAtributo(claseJPA, rawColumn);
				if (aliasNombreReal != null) {
					resultado += referencia + "." + aliasNombreReal.getValue();
				} else {
					resultado += referencia + ".null";
				}
			}
		} else if (condicion.contains("(") && condicion.contains(")")) {
			// Con '(' y ')'

			// Si encontramos una subquery la parseamos y la devolvemos
			if (Utils.isSubquery(condicion)) {
				String subqueryParseada = JPQLTransformerFactory.getInstance(JPQLTransformers.GSP).transform(condicion);
				return Utils.addParentesis(subqueryParseada); // this.transform(condicion);
			}

			// Si empieza y acaba con par�ntesis tenemos IN (2, 'abc') ...
			if (condicion.startsWith("(") && condicion.endsWith(")")) {
				int comienzoParentesis = condicion.indexOf("(");
				int finParentesis = condicion.indexOf(")");
				condicion = condicion.substring(comienzoParentesis + 1, finParentesis);
				Pair<String, String> aliasNombreReal = getTableFromAtributo(condicion);

				if (aliasNombreReal != null) {
					// Tenemos un campo de BD
					resultado += aliasNombreReal.getKey() + "." + aliasNombreReal.getValue();
				} else {
					// Es un literal
					resultado += condicion;
				}
				return Utils.addParentesis(resultado);
			}

			// Si encontramos una funci�n de agregaci�n ...
			String agregatteFunction = this.parseAgregatteFunction(condicion);
			if (agregatteFunction != null) {
				return agregatteFunction;
			}

		} else {
			// Sin .
			// Puede ser un campo BD
			// o
			// Un literal
			Pair<String, String> aliasNombreReal = getTableFromAtributo(condicion);

			if (aliasNombreReal != null) {
				// Tenemos un campo de BD
				resultado += aliasNombreReal.getKey() + "." + aliasNombreReal.getValue();
			} else {
				// Es un literal
				resultado += condicion;
			}
		}

		return resultado;
	}

	private String parseCondition(String condicion, String as) {
		String resultado = this.parseCondition(condicion);
		if (!Utils.isEmpty(as)) {
			resultado += " as " + as;
		}

		return resultado;
	}

	private String parseCondicionExpressions(Set<Condicion> condiciones, String rawExpression) {
		Set<String> condionesToChange = new HashSet<>();

		for (Condicion condicion : condiciones) {
			String izq = condicion.getIzquierda();
			String der = condicion.getDerecha();
			if (!Utils.isEmpty(izq)) {
				if (!izq.startsWith("'") && !izq.startsWith("(") && !Utils.isNumeric(izq)) {
					condionesToChange.add(izq);
				} else if (izq.startsWith("(") && izq.endsWith(")")) {
					condionesToChange.add(izq);
				}
			}
			if (!Utils.isEmpty(der)) {
				if (!der.startsWith("'") && !der.startsWith("(") && !Utils.isNumeric(der)) {
					condionesToChange.add(der);
				} else if (der.startsWith("(") && der.endsWith(")")) {
					condionesToChange.add(der);
				}
			}
		}

		String treatedWhereString = rawExpression;
		for (String condicion : condionesToChange) {

			// Si la condicion parseada es igual a la condici�n original,
			// no hacemos sustituci�n; ejemplo un literal

			String condicionParseada = this.parseCondition(condicion);
			if (condicion.equals(condicionParseada)) {
				continue;
			}

			treatedWhereString = Utils.reemplazarSinLiterales(treatedWhereString, condicion, condicionParseada);
		}

		return treatedWhereString;
	}

	private String parseAgregatteFunction(String rawAggregateFunction) {
		if (rawAggregateFunction == null || rawAggregateFunction.isEmpty()) {
			return null;
		}

		int comienzoParentesis = rawAggregateFunction.indexOf("(");
		int finParentesis = rawAggregateFunction.indexOf(")");

		if (comienzoParentesis == -1 || finParentesis == -1) {
			return null;
		}

		String funcion = rawAggregateFunction.substring(0, comienzoParentesis);
		String contenido = rawAggregateFunction.substring(comienzoParentesis + 1, finParentesis);
		String funcionParseada = FuncionParser.parse(funcion);
		// System.out.println("funcion: " + funcion);
		// System.out.println("contenido: " + contenido);
		// System.out.println("funcionParseada: " + funcionParseada);

		LinkedList<String> parametrosEncontrados = new LinkedList<>();
		String[] parametros = contenido.split(",");
		for (String parametro : parametros) {
			parametro = parametro.trim();
			Pair<String, String> aliasNombreReal = getTableFromAtributo(parametro);
			if (aliasNombreReal != null) {
				// Tenemos un campo de BD
				parametrosEncontrados.add(aliasNombreReal.getKey() + "." + aliasNombreReal.getValue());
			} else {
				// Es un literal
				parametrosEncontrados.add(parametro);
			}
		}

		String contenidoParseado = String.join(", ", parametrosEncontrados);

		return funcionParseada + contenidoParseado + ")";
	}

	private String getTipoJoinString(TipoJoin tipoJoin) {
		return TipoJoin.getJPQLJoin(tipoJoin) + (tipoJoin.equals(TipoJoin.JOIN) ? "" : " JOIN");
	}

	private String getExactMappingAlias(String referencia) {
		for (String key : this.mappingAliasClase.keySet()) {
			if (key.equalsIgnoreCase(referencia)) {
				return key;
			}
		}
		return null;
	}

	private void checkUsoLimitador() {
		for (CampoFrom campoFrom : this.from.getTables()) {
			String alias = campoFrom.getAlias();
			if (alias.matches("[a-z]")) {
				this.diferenciador = DIFERENCIADOR;
				break;
			}
		}
	}

}
