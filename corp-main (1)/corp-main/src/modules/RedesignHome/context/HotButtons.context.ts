import { useState, useCallback } from 'react';

import { createCallableCtx } from 'utils/createCallableContext';

import { Buttons } from '../types/Home.types';

const useHook = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [activeButtons, setActiveButtons] = useState<Set<Buttons>>(new Set(Object.keys(Buttons) as Buttons[]));
  const [formActiveButtons, setFormActiveButtons] = useState<Set<Buttons>>(new Set());

  const openModal = useCallback(() => {
    setIsModalVisible(true);
    setFormActiveButtons(activeButtons);
  }, [activeButtons]);

  const closeModal = useCallback(() => setIsModalVisible(false), []);

  const toggleFormButton = useCallback((button: Buttons) => {
    setFormActiveButtons(prevActiveButtons => {
      const newActiveButtons = new Set(prevActiveButtons);

      if (newActiveButtons.has(button)) {
        newActiveButtons.delete(button);
      } else {
        newActiveButtons.add(button);
      }

      return newActiveButtons;
    });
  }, []);

  const saveNewButtons = useCallback(() => {
    setActiveButtons(formActiveButtons);
    closeModal();
  }, [formActiveButtons, closeModal]);

  const isAllActive = activeButtons.size === Object.keys(Buttons).length;

  return {
    isModalVisible,
    openModal,
    closeModal,
    isAllActive,
    activeButtons,
    formActiveButtons,
    toggleFormButton,
    saveNewButtons,
  };
};

export const [useHotButtons, HotButtonsProvider] = createCallableCtx(useHook, { name: 'HotButtonsProvider' });
