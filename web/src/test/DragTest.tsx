import { useRef, useState } from 'react';
import { DndProvider, useDrag, useDrop } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import update from 'immutability-helper';

interface BlockProps {
  id: string;
  type: string;
  index: number;
  moveBox: (dragIndex: number, hoverIndex: number) => void;
}

const BlockType = {
    BLOCK: 'Block'
}

function Block({ id, type, index, moveBox }: BlockProps) {
    const ref = useRef<HTMLDivElement>(null)

    // 드롭 기능 구현
    const [, drop] = useDrop({
        accept: BlockType.BLOCK,
        hover(item: { index: number }, monitor) {
            if (!ref.current) {
                return;
            }

            const dragIndex = item.index;
            const hoverIndex = index;

            if (dragIndex === hoverIndex) {
                return;
            }

            const hoverBoundingRect = ref.current?.getBoundingClientRect();
            const hoverMiddleY =
                (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;
            const clientOffset = monitor.getClientOffset();

            if (!clientOffset) {
                return;
            }
            const hoverClientY = clientOffset.y - hoverBoundingRect.top;

            if (dragIndex < hoverIndex && hoverClientY < hoverMiddleY) {
                return;
            }
    
            if (dragIndex > hoverIndex && hoverClientY > hoverMiddleY) {
                return;
            }

            moveBox(dragIndex, hoverIndex);
            item.index = hoverIndex;
        },
    })

    // 드래그 기능 구현
    const [{ isDragging }, drag] = useDrag({
        // 드래그하는 아이템의 속성 정의
        type: BlockType.BLOCK,
        item: { id, index },
        collect: (monitor) => ({
            isDragging: monitor.isDragging(),
        }),
    });

    const opacity = isDragging ? 0.4 : 1;

    drag(drop(ref));

    return (
        <div ref={ref} style={{ opacity }}>
            Test
        </div>
    );
}

function Container() {
    const [blocks, setBlocks] = useState([
        { id: '1', text: 'Box 1', type: BlockType.BLOCK },
        { id: '2', text: 'Box 2', type: BlockType.BLOCK },
        { id: '3', text: 'Box 3', type: BlockType.BLOCK },
    ]);

    const moveBox = (dragIndex: number, hoverIndex: number) => {
        const draggedBox = blocks[dragIndex];
        // 새로운 상자 배열 생성하여 이동 처리
        const newBoxes = [...blocks];
        newBoxes.splice(dragIndex, 1);
        newBoxes.splice(hoverIndex, 0, draggedBox);

        // 상태 업데이트로 상자 배열 재설정
        setBlocks(newBoxes);
    }

    
    return (
        // 드래그 앤 드롭을 위한 DndProvider로 컨테이너 감싸기
        <DndProvider backend={HTML5Backend}>
            <div>
                {/* 각 상자 컴포넌트 생성 */}
                {blocks.map((block, index) => (
                    <Block
                        key={block.id}
                        id={block.id}
                        type={block.type}
                        index={index}
                        moveBox={moveBox}
                    />
                ))}
            </div>
        </DndProvider>
     );
}

export default Container;