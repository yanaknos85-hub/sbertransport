import { Button } from 'antd';
import { useTranslation } from 'i18n';
import React, {
  Dispatch, FC, Fragment, useEffect, useState
} from 'react';
import { Icon } from 'shared/components/Icon';
import { Modal } from 'shared/components/Modal/Modal';
import styles from './Filters.module.scss';
import cn from 'classnames';

export const FiltersRegistryWrapper: FC<{
  isVisible?: boolean;
  isNewDesign?: boolean;
  setIsVisible?: Dispatch<React.SetStateAction<boolean>>;
}> = ({
  children, isVisible, isNewDesign, setIsVisible,
}) => {
  const { t } = useTranslation();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isForceRender, setIsForceRender] = useState(false);
  const modalClassName = cn(styles.modal, { [styles.newModal]: isNewDesign });

  useEffect(() => {
    setTimeout(() => setIsForceRender(true), 0);
  }, []);

  const handleOpen = () => (setIsVisible !== undefined ? setIsVisible(true) : setIsModalVisible(true));
  const handleClose = () => (setIsVisible !== undefined ? setIsVisible(false) : setIsModalVisible(false));

  return (
    <Fragment key="Filters">
      <Button
        type="primary"
        className={styles.button}
        onClick={handleOpen}
      >
        <Icon type="control" color="#fff" />
        {t.global.filters}
      </Button>
      <Modal
        centered
        title={t.global.filters}
        footer={() => null}
        visible={isVisible !== undefined ? isVisible : isModalVisible}
        onCancel={handleClose}
        className={modalClassName}
        wrapClassName={modalClassName}
        forceRender={isForceRender}
      >
        {children}
      </Modal>
    </Fragment>
  );
};
