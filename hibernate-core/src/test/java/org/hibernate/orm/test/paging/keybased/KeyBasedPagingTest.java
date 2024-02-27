package org.hibernate.orm.test.paging.keybased;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.query.KeyedResultList;
import org.hibernate.query.Order;
import org.hibernate.query.Page;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SessionFactory
@DomainModel(annotatedClasses = KeyBasedPagingTest.Person.class)
public class KeyBasedPagingTest {
	@Test void test(SessionFactoryScope scope) {
		scope.inTransaction(session -> {
			for (int i = 1; i<18; i++) {
				Person p = new Person();
				p.dob = LocalDate.of(1970, 2, i);
				p.ssn = i*7 + "-" + i*123;
				p.firstName = Integer.toString(i);
				p.lastName = Integer.toString(i);
				session.persist(p);
			}
		});

		scope.inSession(session -> {
			KeyedResultList<Person> first =
					session.createSelectionQuery("from Person", Person.class)
							.getKeyedResultList(Page.first(5)
									.keyedBy(Order.asc(Person.class, "ssn")));
			assertEquals(5,first.getResultList().size());
			int page = 1;
			KeyedResultList<Person> next = first;
			while ( next.getNextPage() != null ) {
				next = session.createSelectionQuery("from Person", Person.class)
						.getKeyedResultList(next.getNextPage());
				assertEquals(page, next.getPage().getPage().getNumber());
				page ++;
			}
			assertEquals(4, page);
			assertEquals( 2, next.getResultList().size());
		});

		scope.inSession(session -> {
			KeyedResultList<Person> first =
					session.createSelectionQuery("from Person", Person.class)
							.getKeyedResultList(Page.first(5)
									.keyedBy(List.of(Order.asc(Person.class, "firstName"),
											Order.asc(Person.class, "lastName"),
											Order.desc(Person.class, "dob"))));
			assertEquals(5,first.getResultList().size());
			int page = 1;
			KeyedResultList<Person> next = first;
			while ( next.getNextPage() != null ) {
				next = session.createSelectionQuery("from Person", Person.class)
						.getKeyedResultList(next.getNextPage());
				assertEquals(page, next.getPage().getPage().getNumber());
				page ++;
			}
			assertEquals(4, page);
			assertEquals( 2, next.getResultList().size());
		});

		scope.inSession(session -> {
			KeyedResultList<Person> first =
					session.createSelectionQuery("from Person where dob > :minDate", Person.class)
							.setParameter("minDate", LocalDate.of(1970, 2, 5))
							.getKeyedResultList(Page.first(5)
									.keyedBy(Order.asc(Person.class, "ssn")));
			assertEquals(5,first.getResultList().size());
			int page = 1;
			KeyedResultList<Person> next = first;
			while ( next.getNextPage() != null ) {
				next = session.createSelectionQuery("from Person where dob > :minDate", Person.class)
						.setParameter("minDate", LocalDate.of(1970, 2, 5))
						.getKeyedResultList(next.getNextPage());
				assertEquals(page, next.getPage().getPage().getNumber());
				page ++;
			}
			assertEquals(3, page);
			assertEquals( 2, next.getResultList().size());
		});
	}

	@Entity(name = "Person")
	static class Person {
		@Id
		String ssn;
		String firstName;
		String lastName;
		LocalDate dob;
	}
}
