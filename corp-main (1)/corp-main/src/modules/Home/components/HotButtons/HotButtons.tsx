import React, { FC, Fragment } from 'react';
import { useTranslation } from 'i18n';

import { HotButton } from 'shared/components/HotButton/HotButton';

import { hotButtons } from '../../constants/hotButtons';
import { useHotButtons } from '../../context/HotButtons.context';

import { Container } from '../Container/Container';
import { ModalCheckbox } from '../ModalCheckbox/ModalCheckbox';

import styles from './HotButtons.module.scss';

export const HotButtons: FC = () => {
  const { t } = useTranslation();

  const {
    openModal,
    activeButtons,
    isModalVisible,
    closeModal,
    saveNewButtons,
    formActiveButtons,
    toggleFormButton,
    isAllActive,
  } = useHotButtons();

  return (
    <Container title={t.HomePage.hotButtons} margin="16px 0 0 0">
      <div className={styles.cards}>
        {hotButtons.map(({
          titleKey, icon: Icon, link, id,
        }) => (
          <Fragment key={id}>
            {activeButtons.has(id) && (
            <HotButton
              title={t.HotButtons[titleKey] as string}
              image={<Icon />}
              link={link}
            />
            )}
          </Fragment>
        ))}
        {!isAllActive && <HotButton isAddVariant onClick={openModal} />}
      </div>
      <ModalCheckbox
        title={t.HotButtons.Modal.title}
        description={t.HotButtons.Modal.description}
        isModalVisible={isModalVisible}
        closeModal={closeModal}
        saveNew={saveNewButtons}
        formActive={formActiveButtons}
        toggleForm={toggleFormButton}
        checkboxes={hotButtons}
        translationModuleKey="HotButtons"
      />
    </Container>
  );
};
