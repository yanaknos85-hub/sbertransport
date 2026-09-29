import * as React from 'react';
import styled from 'styled-components';
import { setLightness } from 'polished';
import Panel from 'components/Panel';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Link } from 'react-router-dom';

const accentColors = ['#27bea4', '#fda500', '#5d9aa7', '#7488fa', '#fd92a7'];

const StyledTitle = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.2px;
  line-height: 16px;
  font-weight: bold;
  color: black;
  justify-content: space-between;
`;

const StyledIconContainer = styled.div<{
  accentColor: string;
}>`
  width: 40px;
  height: 40px;
  border-radius: 20px;
  color: ${({ accentColor }) => setLightness(0.65)(accentColor)};
  background-color: ${({ accentColor }) => setLightness(0.95)(accentColor)};
  margin-left: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
`;

const IconContainer: React.FC<{ icon?: React.ComponentProps<typeof FontAwesomeIcon>['icon']; accentColor: string }> = ({
  icon,
  accentColor,
}) => (
  <StyledIconContainer accentColor={accentColor}>{icon ? <FontAwesomeIcon icon={icon} /> : null}</StyledIconContainer>
);

const Title: React.FC<{
  title: string;
  icon?: React.ComponentProps<typeof FontAwesomeIcon>['icon'];
  accentColor: string;
}> = ({
  title, accentColor, icon,
}) => (
  <StyledTitle>
    {title}
    <IconContainer icon={icon} accentColor={accentColor} />
  </StyledTitle>
);

const CardHeader: React.FC = ({ children }) => <div>{children}</div>;

const StyledCard = styled(Panel)<{ accentColor: string }>`
  min-width: 310px;
  padding: 24px;
  margin: 8px;
  height: auto;
  display: flex;
  flex-direction: column;
  color: var(--ship-cove);
  font-size: 14px;
  line-height: 22px;
  justify-content: space-between;

  a,
  a:visited,
  a:active {
    color: inherit;
  }
`;

export const CardBody = styled.div`
  margin-top: 24px;
`;

export const Card: React.FC<
  {
    title: string;
    titleLink?: string;
    accentColor?: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    icon?: any;
  } & React.ComponentProps<typeof StyledCard>
> = ({
  title, titleLink, icon, children, accentColor = '#666666', ...rest
}) => {
  const titleElement = (
    <Title
      title={title}
      icon={icon}
      accentColor={accentColor}
    />
  );

  return (
    <StyledCard {...{ accentColor, ...rest }}>
      <CardHeader>{titleLink ? <Link to={titleLink}>{titleElement}</Link> : titleElement}</CardHeader>
      {children ? <CardBody>{children}</CardBody> : null}
    </StyledCard>
  );
};

const StyledHomeCards = styled.div`
  display: flex;
  flex-wrap: wrap;
`;

export const Cards = ({ children }: { children: JSX.Element | JSX.Element[] }): JSX.Element => (
  <StyledHomeCards>
    {React.Children.map(children, (c, i) => React.cloneElement<React.ComponentProps<typeof Card>>(c, {
      accentColor: accentColors[i % accentColors.length],
    })
    )}
  </StyledHomeCards>
);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const LinkCard: React.FC<{ to: string; title: string; icon?: any }> = ({
  to,
  title,
  ...props
}) => (
  <Card
    title={title}
    titleLink={to}
    {...props}
  />
);
