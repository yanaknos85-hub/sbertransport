import React, { Component, FC, ReactNode } from 'react';
import Title from 'antd/lib/typography/Title';
import Text from 'antd/lib/typography/Text';
import Paragraph from 'antd/lib/typography/Paragraph';
import styled from 'styled-components';

import { useAppStore } from 'ioc';
import { getErrorCode } from 'utils/getErrorCode';
import { isTestMode } from 'utils/isTestMode';
import {
  CustomErrorCode, errorText, SUPPORT_PHONE, SDO_SUPPORT_PHONE
} from 'constants/app.constants';

import { ReactComponent as Reload } from 'assets/icons/reload.svg';
import Cat from 'assets/images/cat.png';
import { Button } from '../Button';
import styles from './index.module.scss';

interface State {
  error: Error | null;
}

interface Props {
  fallback?: React.FC<{ error: State['error']; reset: () => void }>;
  catchUnhandled?: boolean;
}

const Stacktrace = styled.pre`
  font-size: x-small;
  max-height: 25vh;
  background-color: #444 !important;
  color: #66aa44;
  padding: 1ex;
  text-align: left;
  margin-top: 15px;
`;

export const DefaultFallback: FC<{
  code?: number;
  error: Error | null;
  reset?: () => void;
  reload?: () => void;
}> = ({
  code: customCode, error, reload,
}) => {
  const code = customCode ?? getErrorCode(error);
  const { configStore } = useAppStore();
  const IS_TEST_MODE = isTestMode();

  const phone = configStore.env.IS_SDO ? SDO_SUPPORT_PHONE : SUPPORT_PHONE;
  const sdoLimitedAccess = configStore.env.IS_SDO && code === 403;

  return (
    <div className={styles.defaultWrapper}>
      <div className={styles.defaultErrorContainer}>
        <img
          src={Cat}
          className={styles.errorImg}
          alt="Сберкот"
        />

        <Title level={5}>{errorText[code]?.title ?? errorText[CustomErrorCode.UNKNOWN].title}</Title>
        {!sdoLimitedAccess && (
          <>
            <Text>{errorText[code]?.subtitle ?? errorText[CustomErrorCode.UNKNOWN].subtitle}</Text>
            <a href={`tel:${phone.code}`} className={styles.phone}>
              {phone.title}
            </a>
            <Paragraph type="secondary" className={styles.code}>
              Ошибка
              {code}
            </Paragraph>
          </>
        )}

        {reload && !sdoLimitedAccess && (
          <Button
            onClick={reload}
            type="primary"
            className={styles.reloadButton}
            icon={<Reload />}
            block
          >
            Перезагрузить
          </Button>
        )}
      </div>

      {IS_TEST_MODE && error?.stack && (
        <Stacktrace>
          {error?.message || null}
          {'\n\n'}
          {error?.stack}
        </Stacktrace>
      )}
    </div>
  );
};

export default class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      error: null,
    };
  }

  componentDidMount(): void {
    const { catchUnhandled = false } = this.props;

    if (catchUnhandled) {
      window.addEventListener('unhandledrejection', this.onUnhandledRejection);
    }
  }

  componentWillUnmount(): void {
    const { catchUnhandled = false } = this.props;

    if (catchUnhandled) {
      window.removeEventListener('unhandledrejection', this.onUnhandledRejection);
    }
  }

  onUnhandledRejection = (event: PromiseRejectionEvent): void => {
    event.stopPropagation();

    event.promise.catch(error => this.setState(ErrorBoundary.getDerivedStateFromError(error)));
  };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  reset = (): void => this.setState({ error: null });

  reload = (): void => window.location.reload();

  render(): ReactNode {
    const { error } = this.state;
    const { children, fallback: Fallback = DefaultFallback } = this.props;

    return error ? (
      <Fallback
        error={error}
        reset={this.reset}
        reload={this.reload}
      />
    ) : children;
  }
}
