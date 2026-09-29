import React, { createContext, MutableRefObject, useRef } from 'react';
import classNames from 'classnames';

import styles from './styles.module.scss';

export const LoginWrapperContext = createContext<MutableRefObject<any> | null>(null);

export const LoginWrapper: React.FC<any> = ({ children }) => {
  const ref = useRef(null);

  return (
    <div id="mobile-wrapper-container" className={classNames(styles.wrapper, 'mobile-wrapper')}>
      <div className={styles.innerWrapper} ref={ref}>
        <div className={styles.inner}>
          <LoginWrapperContext.Provider value={ref}>{children}</LoginWrapperContext.Provider>
        </div>
      </div>
    </div>
  );
};
