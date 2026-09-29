import { PageHeader } from 'antd';
import React, { FC } from 'react';

import { PageContent } from '../PageContent/PageContent';
import { SpinWrapped } from '../SpinWrapped/SpinWrapped';

interface TPageLayoutProps {
  children?: React.ReactNode;
  empty?: React.ReactNode;
  tags?: React.ReactElement;
  extra?: React.ReactNode;
  title: string;
  onBack?(): void;
  loading?: boolean;
  contentClassName?: string;
}

const PageLayout: FC<TPageLayoutProps> = ({
  children,
  empty,
  title,
  onBack,
  tags,
  loading = false,
  extra,
  contentClassName,
}) => loading ? (
  <div style={{ paddingTop: 50 }}>
    <SpinWrapped />
  </div>
) : (
  <>
    <PageHeader
      className="custom-header"
      title={title}
      onBack={onBack}
      tags={tags}
      extra={extra}
    />
    <PageContent className={contentClassName}>{children ?? empty ?? null}</PageContent>
  </>
);

export default PageLayout;
