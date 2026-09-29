import React, {
  Dispatch, FC, MutableRefObject, ReactNode
} from 'react';
import { FiltersRegistryWrapper } from '../Filters/Filters';

import styles from './ActionsRegistry.module.scss';

interface Props {
  idValue?: string;
  handleSearchId?: (id: string) => void;
  buttons?: ReactNode[];
  filters?: ReactNode;
  isVisible?: boolean;
  setIsVisible?: Dispatch<React.SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  isNewDesign?: boolean;
  handleChangeId?: (id: string) => void;
}

export const ActionsRegistry: FC<Props> = ({
  buttons,
  filters,
  isVisible,
  setIsVisible,
  buttonsRef,
  isNewDesign,
}) => {
  return (
    <div className={styles.container}>
      {buttonsRef && <div ref={buttonsRef} />}

      <div className={styles.controls}>
        <div className={styles.searchContainer}>
          <FiltersRegistryWrapper
            isNewDesign={isNewDesign}
            isVisible={isVisible}
            setIsVisible={setIsVisible}
          >
            {filters}
          </FiltersRegistryWrapper>
        </div>
        <div className={styles.buttons}>
          {buttons}
        </div>
      </div>
    </div>
  );
};
