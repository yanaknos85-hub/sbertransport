import React, { useState, FC } from 'react';

import { useModalState } from 'hooks/useModal';
import { IInstruction } from 'types/Instructions/Instructions.interface';
import Story from '../Story/Story';

import {
  FullScreenContent, InstructionTitle, InstructionsContainer, Instruction
} from './InstructionsItemsStyled';

interface InstructionsItemsProps {
  instructions: IInstruction[];
}

const InstructionsItems: FC<InstructionsItemsProps> = ({ instructions }) => {
  const [selectedInstruction, setSelectedInstruction] = useState<IInstruction | null>(null);
  const [modal, modalActions] = useModalState(false);

  const selectedItem = (instruction: IInstruction) => {
    if (instruction.link) {
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      window.open(require(`assets/images/instructions/${instruction.link}`), '_blank');
      return;
    }

    setSelectedInstruction(instruction);
    modalActions.show();
  };

  return (
    <InstructionsContainer>
      {instructions.map(instruction => (
        <Instruction
          key={instruction.title}
          cover={instruction.coverImage}
          onClick={() => selectedItem(instruction)}
        >
          <InstructionTitle>{instruction.title}</InstructionTitle>
        </Instruction>
      ))}
      {modal && (
        <FullScreenContent>
          <Story onCancel={modalActions.hide} instruction={selectedInstruction!} />
        </FullScreenContent>
      )}
    </InstructionsContainer>
  );
};

export default InstructionsItems;
