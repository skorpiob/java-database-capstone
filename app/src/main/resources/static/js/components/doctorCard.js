/*
Import the overlay function for booking appointments from loggedPatient.js
*/
import { showBookingOverlay } from "./loggedPatient.js";

/*
Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js
*/
import { deleteDoctor } from "../services/doctorServices.js";

/*
Import function to fetch patient details (used during booking) from patientServices.js
*/
import { getPatientDetails } from "../services/patientServices.js";

/*
Function to create and return a DOM element for a single doctor card
*/
export function createDoctorCard(doctor) {

  /*
    Create the main container for the doctor card
  */
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  /*
    Retrieve the current user role from localStorage
  */
  const role = localStorage.getItem("userRole");

  /*
    Create a div to hold doctor information
  */
  const infoContainer = document.createElement("div");
  infoContainer.classList.add("doctor-info");

  /*
    Create and set the doctor’s name
  */
  const name = document.createElement("h3");
  name.textContent = doctor.name;

  /*
    Create and set the doctor's specialization
  */
  const specialization = document.createElement("p");
  specialization.textContent = `Specialization: ${doctor.specialization}`;

  /*
    Create and set the doctor's email
  */
  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  /*
    Create and list available appointment times
  */
  const times = document.createElement("p");
  if (doctor.availableTimes && doctor.availableTimes.length > 0) {
    times.textContent = `Available: ${doctor.availableTimes.join(", ")}`;
  } else {
    times.textContent = "No available appointments";
  }

  /*
    Append all info elements to the doctor info container
  */
  infoContainer.appendChild(name);
  infoContainer.appendChild(specialization);
  infoContainer.appendChild(email);
  infoContainer.appendChild(times);

  /*
    Create a container for card action buttons
  */
  const actionsContainer = document.createElement("div");
  actionsContainer.classList.add("doctor-actions");

  /*
    === ADMIN ROLE ACTIONS ===
  */
  if (role === "admin") {

    /*
      Create a delete button
    */
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "Delete";
    deleteBtn.classList.add("delete-btn");

    /*
      Add click handler for delete button
    */
    deleteBtn.addEventListener("click", async () => {

      /*
        Get the admin token from localStorage
      */
      const token = localStorage.getItem("token");

      try {

        /*
          Call API to delete the doctor
        */
        await deleteDoctor(doctor.id, token);

        /*
          Show result and remove card if successful
        */
        alert("Doctor deleted successfully");
        card.remove();

      } catch (error) {
        alert("Failed to delete doctor");
        console.error(error);
      }
    });

    /*
      Add delete button to actions container
    */
    actionsContainer.appendChild(deleteBtn);
  }

  /*
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
  */
  else if (role === "patient") {

    /*
      Create a book now button
    */
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.classList.add("book-btn");

    /*
      Alert patient to log in before booking
    */
    bookBtn.addEventListener("click", () => {
      alert("Please log in before booking an appointment.");
    });

    /*
      Add button to actions container
    */
    actionsContainer.appendChild(bookBtn);
  }

  /*
    === LOGGED-IN PATIENT ROLE ACTIONS ===
  */
  else if (role === "loggedPatient") {

    /*
      Create a book now button
    */
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.classList.add("book-btn");

    /*
      Handle booking logic for logged-in patient
    */
    bookBtn.addEventListener("click", async () => {

      /*
        Redirect if token not available
      */
      const token = localStorage.getItem("token");
      if (!token) {
        window.location.href = "/";
        return;
      }

      try {

        /*
          Fetch patient data with token
        */
        const patient = await getPatientDetails(token);

        /*
          Show booking overlay UI with doctor and patient info
        */
        showBookingOverlay(doctor, patient);

      } catch (error) {
        console.error("Error fetching patient details:", error);
        alert("Unable to fetch patient details.");
      }
    });

    /*
      Add button to actions container
    */
    actionsContainer.appendChild(bookBtn);
  }

  /*
    Append doctor info and action buttons to the card
  */
  card.appendChild(infoContainer);
  card.appendChild(actionsContainer);

  /*
    Return the complete doctor card element
  */
  return card;
}