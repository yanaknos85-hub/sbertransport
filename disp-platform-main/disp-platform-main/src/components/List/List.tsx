import React, {
  FC, useState, useEffect, useCallback
} from 'react';
import { Pagination } from '@sber-sbertransport/ui-kit/src';

import { useQuery } from 'hooks/useQuery';
import { ButtonAdd } from './components/ButtonAdd/ButtonAdd';
import { ButtonLoad } from './components/ButtonLoad/ButtonLoad';
import { Row, Props as RowProps } from './components/Row/Row';
import { ModalFilters, Props as ModalFilterProps } from './components/ModalFilters/ModalFilters';
import { SearchPanel, SearchPanelProps } from './components/SearchPanel/SearchPanel';
import { RowData } from './types';
import styles from './List.module.scss';

type Query = Record<string, unknown>;

type Rows = RowProps<RowData>[];

export interface Props {
  autoSize?: boolean;
  onAddClick?: () => void;
  formJSX: ModalFilterProps<Query>['formJSX'];
  rows: Rows;
  totalElements: number;
  onChangeQuery: (query: Query) => void;
  onExportClick?: () => void;
  onImportClick?: () => void;
  searchPanelProps?: Omit<SearchPanelProps, 'setQuery'>;
  defaultQuery?: Query;
}

export const List: FC<Props> = ({
  autoSize,
  onAddClick,
  formJSX,
  rows,
  totalElements,
  onChangeQuery,
  onExportClick,
  onImportClick,
  searchPanelProps,
  defaultQuery,
}) => {
  const {
    query, setQuery, setPagination,
  } = useQuery<Query>(defaultQuery);
  const [tableHeight, setTableHeight] = useState(380);

  useEffect(() => {
    onChangeQuery(query);
  }, [onChangeQuery, query]);

  const resizeTable = useCallback(() => {
    const layout = document.querySelector('[class*=PageLayout')?.getBoundingClientRect().height || 0;

    if (!layout) {
      return setTimeout(resizeTable);
    }

    const contentHeight = layout - (onAddClick ? 340 : 240);

    setTableHeight(contentHeight);
  }, [onAddClick]);

  useEffect(() => {
    if (autoSize) {
      resizeTable();
    }
  }, [autoSize, resizeTable]);

  useEffect(() => {
    window.addEventListener('resize', resizeTable);

    return () => {
      window.removeEventListener('resize', resizeTable);
    };
  }, [resizeTable]);

  return (
    <div className={styles.list}>
      <div className={styles.tableHeader}>
        <SearchPanel setQuery={setQuery} {...searchPanelProps} />
        <ModalFilters
          query={query}
          setQuery={setQuery}
          formJSX={formJSX}
        />
        <div className={styles.loadGroup}>
          <ButtonLoad
            key="export"
            type="export"
            onClick={onExportClick}
          />
          <ButtonLoad
            key="import"
            type="import"
            onClick={onImportClick}
          />
        </div>
      </div>
      <div className={styles.rows} style={{ height: tableHeight }}>
        {rows.map(row => (
          <Row key={row.rowData.id} {...row} />
        ))}
      </div>
      {onAddClick && (
        <div className={styles.button}>
          <ButtonAdd onClick={onAddClick} />
        </div>
      )}
      <div className={styles.pagination}>
        <Pagination
          pagination={query}
          total={totalElements}
          setPagination={setPagination}
        />
      </div>
    </div>
  );
};
