import CSVSourceNode from './CSVSourceNode';
import SqlSourceNode from './SqlSourceNode';
import CsvOutputNode from './CsvOutputNode';
import FilterNode from './FilterNode';
import MapNode from './MapNode';
import GroupByNode from './GroupByNode';
import JoinNode from './JoinNode';
import SortNode from './SortNode';
import SQLOutputNode from './SQLOutputNode';

export const nodeTypes = {
  csvSource: CSVSourceNode,
  sqlSource: SqlSourceNode,
  mysqlSource: SqlSourceNode,
  postgresqlSource: SqlSourceNode,
  sqliteSource: SqlSourceNode,
  filter: FilterNode,
  map: MapNode,
  groupBy: GroupByNode,
  join: JoinNode,
  sort: SortNode,
  sqlOutput: SQLOutputNode,
  csvOutput: CsvOutputNode,
};
