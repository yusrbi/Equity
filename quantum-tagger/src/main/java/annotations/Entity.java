package annotations;


public class Entity extends Annotation{
	

	public Entity(int rowIndx, int columnIndx, int startOffset, int endOffset,
			Annotation.ANNOTATION  annotation) {
		super(rowIndx, columnIndx, startOffset, endOffset, annotation);
		
	}

	

}
