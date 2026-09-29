import React, {
  Component, FC, PropsWithChildren, ReactNode
} from 'react';

import { getErrorCode } from 'utils/getErrorCode';
import {
  Contacts,
  CustomErrorCode, errorText
} from 'constants/app.constants';

import styles from './index.module.scss';
import Button from 'components/Button/Button';

interface State {
  error: Error | null;
}

interface Props {
  fallback?: React.FC<{ error: State['error']; reset: () => void }>;
  catchUnhandled?: boolean;
}

export const DefaultFallback: FC<{
  code?: number;
  error: Error | null;
  reset?: () => void;
  reload?: () => void;
}> = ({
  code: customCode, error, reload,
}) => {
  const code = customCode ?? getErrorCode(error);

  return (
    <div className={styles.defaultWrapper}>
      <div className={styles.defaultErrorContainer}>
        <div className={styles.title}>{errorText[code]?.title ?? errorText[CustomErrorCode.UNKNOWN].title}</div>
        <div>{errorText[code]?.subtitle ?? errorText[CustomErrorCode.UNKNOWN].subtitle}</div>
        <a href={Contacts.linkOutTel} className={styles.phone}>
          {Contacts.outTel}
        </a>
        <div className={styles.code}>
          Ошибка
          {' '}
          {code}
        </div>

        {reload && (
        <Button
          onClick={reload}
          color="primary"
          className={styles.reloadButton}
          block
        >
          Перезагрузить
        </Button>
        )}
      </div>

      {/* {IS_TEST_MODE && error?.stack && (
        <div className={styles.stacktrace}>
          {error?.message || null}
          {'\n\n'}
          {error?.stack}
        </div>
      )} */}
    </div>
  );
};

export default class ErrorBoundary extends Component<PropsWithChildren<Props>, State> {
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
