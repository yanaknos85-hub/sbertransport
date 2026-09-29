import React, {
  useState, useEffect, useMemo, FC
} from 'react';
import { Progress } from 'antd';

import Close from 'assets/icons/close-white.svg';
import { IInstruction } from 'types/Instructions/Instructions.interface';

import {
  CloseButton,
  DescriptionText,
  DescriptionTitle,
  MainImage,
  ProgressContainer,
  ProgressItem,
  StoryHeader,
  WrapperDescription,
  WrapperMainImage,
  WrapperStory,
  WrapperInfoStory
} from './StoryStyled';

interface StoryProps {
  onCancel(): void;
  instruction: IInstruction;
}

const Story: FC<StoryProps> = ({ onCancel, instruction }) => {
  const [currentStory, setCurrentStory] = useState(0);
  const [progress, setProgress] = useState(0);
  const [autoplay, setAutoplay] = useState(true);

  const stories = useMemo(() => instruction.pages.map(({
    image, title, text,
  }, index) => ({
    id: index,
    src: image,
    title: title,
    type: 'image',
    text,
    duration: 5,
  })),
  [instruction.pages]);

  useEffect(() => {
    let interval;

    if (autoplay) {
      interval = setInterval(() => {
        setProgress(prevProgress => {
          if (prevProgress >= 100) {
            clearInterval(interval);
            if (currentStory < stories.length - 1) {
              setCurrentStory(currentStory + 1);
              setProgress(0);
            } else {
              setAutoplay(false);
            }

            return 100;
          } else {
            return prevProgress + (100 / stories[currentStory]?.duration / 10);
          }
        });
      }, 100);
    }

    return () => {
      clearInterval(interval);
    };
  }, [currentStory, autoplay, stories]);

  const nextStory = () => {
    if (currentStory < stories.length - 1) {
      setCurrentStory(currentStory + 1);
      setProgress(0);
      setAutoplay(true);
    }
  };

  const prevStory = () => {
    if (currentStory > 0) {
      setCurrentStory(currentStory - 1);
      setProgress(0);
      setAutoplay(true);
    }
  };

  const changeHistory = (index: number) => {
    if (index > currentStory) {
      nextStory();
    } else if (index !== currentStory) {
      prevStory();
    }
  };

  return (
    <WrapperStory>
      <StoryHeader>
        <ProgressContainer>
          {stories.map((story, index) => (
            <ProgressItem onClick={() => changeHistory(index)} key={story.id}>
              <Progress
                percent={currentStory === index ? progress : currentStory > index ? 100 : 0}
                showInfo={false}
                status={currentStory === index ? 'active' : 'normal'}
              />
            </ProgressItem>
          ))}
        </ProgressContainer>
        <CloseButton src={Close} onClick={onCancel} />
      </StoryHeader>
      <WrapperMainImage>
        <WrapperInfoStory>
          <MainImage
            src={require(`assets/images/instructions/${stories[currentStory]?.src}`)}
            onClick={nextStory}
          />
          <WrapperDescription>
            <DescriptionTitle>
              {stories[currentStory]?.title}
            </DescriptionTitle>
            <DescriptionText>
              {stories[currentStory]?.text}
            </DescriptionText>
          </WrapperDescription>
        </WrapperInfoStory>
      </WrapperMainImage>
    </WrapperStory>
  );
};

export default Story;
