import React, {
  Dispatch, FC, MutableRefObject, ReactNode
} from 'react';
import styles from './ActionsRegistry.module.scss';
import { FiltersRegistryWrapper } from 'modules/BusinessReports/components/Filters/Filters';

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
  countActiveFilters?: number;
  handleResetFilters?: () => void;
}

export const ActionsRegistry: FC<Props> = ({
  buttons,
  filters,
  isVisible,
  setIsVisible,
  buttonsRef,
  isNewDesign,
  countActiveFilters,
  handleResetFilters,
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
            countActiveFilters={countActiveFilters}
            handleResetFilters={handleResetFilters}
          >
            {filters}
          </FiltersRegistryWrapper>
        </div>
        <div className={styles.buttons} style={{ justifyContent: 'end' }}>
          {buttons}
        </div>
      </div>
    </div>
  );
};
