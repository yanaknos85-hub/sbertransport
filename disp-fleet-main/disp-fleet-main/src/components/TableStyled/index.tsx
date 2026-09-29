import { Table } from 'antd';
import { TableProps } from 'antd/lib/table';
import React, {
  FC, useEffect, useRef, useState
} from 'react';
import cn from 'classnames';
import { Pagination } from '@sber-sbertransport/ui-kit/src';
import styles from './index.module.scss';
import { PaginationParams } from 'utils/io-ts/pagination';
import { EmptyView } from 'components/EmptyView/EmptyView';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface ITableStyled extends TableProps<any> {
  paginationParams: PaginationParams;
  total: number;
  setPagination: (pagination: PaginationParams) => void;
  /** Высота таблицы подстраивается под высоту свободного пространства, чтобы у страницы не было скролла */
  autoHeight?: boolean;
  secondStyle?: boolean; // Хз, как назвать этот стиль. В ките разберемся с неймингом
}

const tableLocale: ITableStyled['locale'] = {
  emptyText() {
    return <EmptyView description="Нет данных" maxWidth={250} />;
  },
};

export const TableStyled: FC<ITableStyled> = ({
  paginationParams,
  total,
  setPagination,
  className,
  scroll,
  autoHeight = false,
  secondStyle,
  children,
  ...props
}) => {
  const tableWrapper = useRef<HTMLDivElement>(null);
  const paginationWrapper = useRef<HTMLDivElement>(null);
  const childrenRef = useRef<HTMLDivElement>(null);

  const [tableHeight, setTableHeight] = useState(0);

  useEffect(() => {
    if (!autoHeight) return;

    const resize = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const topOffset = tableWrapper.current?.getBoundingClientRect().top ?? 0 + scrollTop;
      const viewportHeight = window.innerHeight;
      const paginationHeight = paginationWrapper.current?.offsetHeight || 0;
      const tableMarginBottom = 10; // Отступ между таблицей и пагинацией
      const pagePadding = 16;
      const antdTableHeaderHeight = tableWrapper.current?.querySelector('.ant-table-header')?.clientHeight || 0;
      const childrenHeight = childrenRef.current?.offsetHeight || 0;
      const otherPaddings = 1; // Без этого пикселя на некоторых экранах появляется скролл

      setTableHeight(
        viewportHeight - topOffset - antdTableHeaderHeight - paginationHeight
        - tableMarginBottom - pagePadding - childrenHeight - otherPaddings
      );
    };

    resize();

    window.addEventListener('resize', resize);

    return () => {
      window.removeEventListener('resize', resize);
    };
  }, [autoHeight]);

  const antdTableHeaderHeight = tableWrapper.current?.querySelector('.ant-table-header')?.clientHeight || 0;

  return (
    <>
      <div ref={tableWrapper}>
        <Table
          pagination={false}
          className={cn({
            [styles.table]: !secondStyle,
            [styles.secondStyleTable]: secondStyle,
          }, className)}
          scroll={{
            ...scroll,
            y: autoHeight ? tableHeight || 'auto' : scroll?.y,
          }}
          style={{
            minHeight: autoHeight ? tableHeight + antdTableHeaderHeight : 'none',
            ...props.style,
          }}
          locale={tableLocale}
          {...props}
        />
      </div>

      {!!children && <div ref={childrenRef}>{children}</div>}

      <div ref={paginationWrapper}>
        <Pagination
          pagination={paginationParams}
          total={total}
          setPagination={setPagination}
        />
      </div>
    </>
  );
};
