import React, { Fragment, FC, ReactNode } from 'react';
import { EmptyView } from 'components/EmptyView/EmptyView';
import { DetailedHeader, Props as DetailedHeaderProps } from './components/DetailedHeader/DetailedHeader';
import { InfoPanel, Props as InfoPanelProps } from './components/InfoPanel/InfoPanel';
import { RowData } from './types';

export type Props = {
  activeRow?: RowData | null;
  emptyProps?: {
    title?: string;
    description?: string;
  };
  content?: ReactNode;
} & DetailedHeaderProps &
InfoPanelProps;

export const ListDetailed: FC<Props> = ({
  activeRow, emptyProps, items, content, ...headerProps
}) => {
  if (!activeRow) {
    return <EmptyView {...emptyProps} />;
  }

  return (
    <Fragment key="ListDetailed">
      <DetailedHeader activeRow={activeRow} {...headerProps} />
      <InfoPanel items={items} />
      {content}
    </Fragment>
  );
};
