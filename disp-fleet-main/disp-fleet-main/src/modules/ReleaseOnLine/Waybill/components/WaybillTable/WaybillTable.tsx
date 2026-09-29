import React, { FC } from 'react';

import { Link } from 'react-router-dom';

import { useProfile } from 'api/profile/profile.api';
import { useSearchWaybill } from 'api/waybill/waybill.api';
import { WaybillFilters } from 'api/waybill/waybill.types';

import { WAYBILL } from 'constants/routes.constants';

import { PaginationParams } from 'utils/io-ts/pagination';

import { Button } from 'components/Button';
import { TableStyled } from 'components/TableStyled';

import { useColumns } from './hooks/useColumns';

import styles from './WaybillTable.module.scss';

interface Props {
  query: WaybillFilters;
  setPagination: (pagination: PaginationParams) => void;
}

const WaybillTable: FC<Props> = ({ query, setPagination }) => {
  const { ewbCreationPossibility } = useProfile().data;

  const { columns } = useColumns();

  const { content: data, totalElements } = useSearchWaybill(query).data;

  return (
    <div>
      <TableStyled
        dataSource={data}
        columns={columns}
        paginationParams={query}
        setPagination={setPagination}
        total={totalElements}
        autoHeight
      >
        {ewbCreationPossibility && (
          <div className={styles.createButtonWrapper}>
            <Link to={WAYBILL}>
              <Button type="primary">Создать ЭПЛ</Button>
            </Link>
          </div>
        )}
      </TableStyled>
    </div>
  );
};

export default WaybillTable;
