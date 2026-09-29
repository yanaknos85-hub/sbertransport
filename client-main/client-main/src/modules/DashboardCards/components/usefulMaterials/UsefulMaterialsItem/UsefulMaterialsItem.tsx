import React, { useState, useMemo } from 'react';
import type { FC } from 'react';

import {
  FullScreenContent,
  UsefulMaterialsItemTitle,
  WrapperUsefulMaterialsItem
} from './styled';
import { ServiceType } from 'modules/DashboardCards/constants/general.constants';
import type { IInstruction, IInstructions } from 'modules/DashboardCards/types/usefulMaterials.types';
import Story from './Story/Story';
import { useModalState } from 'shared/hooks/useModal';
import Background from 'shared/components/Images/backgroundInstruction.png';

interface UsefulMaterialsItemsProps {
  filter: string;
  allInstructions: IInstructions;
}

const UsefulMaterialsItems: FC<UsefulMaterialsItemsProps> = ({ filter, allInstructions }) => {
  const [selectedInstruction, setSelectedInstruction] = useState<IInstruction | null>(null);
  const [modal, modalActions] = useModalState(false);
  const instructions = useMemo(() => filter === ServiceType.ALL
    ? allInstructions.services.flatMap(({ instructions }) => instructions)
    : allInstructions.services.find(({ serviceName }) => serviceName === filter)?.instructions ?? [],
  [filter, allInstructions]);

  const selectedItem = (instruction: IInstruction) => {
    setSelectedInstruction(instruction);
    modalActions.show();
  };

  return (
    <>
      {instructions.map(instruction => (
        <WrapperUsefulMaterialsItem cover={instruction.coverImage} onClick={() => selectedItem(instruction)}>
          <UsefulMaterialsItemTitle>{instruction.title}</UsefulMaterialsItemTitle>
        </WrapperUsefulMaterialsItem>
      ))}
      {modal && (
        <FullScreenContent background={Background}>
          <Story onCancel={modalActions.hide} instruction={selectedInstruction} />
        </FullScreenContent>
      )}
    </>
  );
};

export default UsefulMaterialsItems;
